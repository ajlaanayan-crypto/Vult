package com.ayan.vult.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ayan.vult.data.AppDatabase
import com.ayan.vult.data.SecurityDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class VultMonitoringService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    private lateinit var securityDataStore: SecurityDataStore
    
    private var lastUnlockedPackage: String? = null
    private var lastUnlockTime: Long = 0
    private var dynamicUnlockDuration = 60000L

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.ayan.vult.ACTION_UNLOCK") {
                lastUnlockedPackage = intent.getStringExtra("PACKAGE_NAME")
                lastUnlockTime = System.currentTimeMillis()
                Log.d("VultMonitoring", "Session unlocked for: $lastUnlockedPackage")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        securityDataStore = SecurityDataStore(this)
        
        val filter = IntentFilter("com.ayan.vult.ACTION_UNLOCK")
        registerReceiver(unlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        
        // Initial load of duration
        serviceScope.launch {
            dynamicUnlockDuration = securityDataStore.unlockDuration.first()
        }

        Log.d("VultMonitoring", "Service Created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("VultMonitoring", "Service Connected")
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                         AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 50
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        // We listen to content changes too for real-time node searching in settings
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val packageName = event.packageName?.toString() ?: return
            
            // Periodically refresh duration preference
            serviceScope.launch {
                dynamicUnlockDuration = securityDataStore.unlockDuration.first()
            }

            if (packageName == "com.android.systemui") return

            // Skip if within the unlock window
            if (packageName == lastUnlockedPackage && (System.currentTimeMillis() - lastUnlockTime) < dynamicUnlockDuration) {
                return
            }

            // AGGRESSIVE ANTI-TAMPER: Detect if user is trying to uninstall Vult or disable service
            if (isAntiTamperTriggered(packageName)) {
                Log.w("VultMonitoring", "Anti-Tamper triggered for: $packageName")
                launchOverlay(this.packageName, "Vult Security (Tamper Protected)")
                return
            }

            // SPECIAL CASE: Vult itself always locks (unless recently unlocked)
            if (packageName == this.packageName) {
                if (Settings.canDrawOverlays(this)) {
                    launchOverlay(packageName, "Vult Dashboard")
                }
                return
            }

            // Only check database for other apps if it's a window change event (performance)
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
                eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
                serviceScope.launch {
                    val isBlocked = database.blockedAppDao().isAppBlocked(packageName)
                    if (isBlocked) {
                        if (Settings.canDrawOverlays(this@VultMonitoringService)) {
                            launchOverlay(packageName)
                        }
                    } else {
                        if (packageName != lastUnlockedPackage) {
                            lastUnlockedPackage = null
                        }
                    }
                }
            }
        }
    }

    private fun isAntiTamperTriggered(packageName: String): Boolean {
        // 1. Settings Protection (App Info & Accessibility)
        if (packageName == "com.android.settings") {
            val rootNode = rootInActiveWindow ?: return false
            
            // Check if viewing Vult App Info (to uninstall or clear data)
            val isVultAppInfo = findNodeByText(rootNode, "Vult") && 
                                (findNodeByText(rootNode, "Uninstall") || 
                                 findNodeByText(rootNode, "Storage") || 
                                 findNodeByText(rootNode, "Clear data") ||
                                 findNodeByText(rootNode, "Force stop"))
            
            // Check if viewing Accessibility Settings to disable the service
            val isAccessibilityTamper = findNodeByText(rootNode, "Accessibility") && 
                                       (findNodeByText(rootNode, "Vult") || 
                                        findNodeByText(rootNode, "Vult Security Monitor"))
            
            return isVultAppInfo || isAccessibilityTamper
        }
        
        // 2. Package Installer Protection (Aggressive check)
        if (packageName.contains("packageinstaller", ignoreCase = true)) {
            val rootNode = rootInActiveWindow ?: return false
            return findNodeByText(rootNode, "Vult")
        }
        
        return false
    }

    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) return true
        if (node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true) return true

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findNodeByText(child, text)) return true
        }
        return false
    }

    private fun launchOverlay(packageName: String, customAppName: String? = null) {
        val appName = customAppName ?: try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val intent = Intent(this, SecurityOverlayService::class.java).apply {
            putExtra("PACKAGE_NAME", packageName)
            putExtra("APP_NAME", appName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startService(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(unlockReceiver)
        serviceScope.cancel()
    }
}
