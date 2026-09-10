package com.ayan.vult.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.savedstate.*
import com.ayan.vult.data.SecurityDataStore
import com.ayan.vult.ui.theme.VultTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class SecurityOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private var currentPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentPackageName = intent?.getStringExtra("PACKAGE_NAME")
        val appName = intent?.getStringExtra("APP_NAME") ?: "Protected App"
        
        if (overlayView == null) {
            showOverlay(appName)
        }
        
        return START_NOT_STICKY
    }

    private fun showOverlay(appName: String) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            this.flags = this.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@SecurityOverlayService)
            setViewTreeViewModelStoreOwner(this@SecurityOverlayService)
            setViewTreeSavedStateRegistryOwner(this@SecurityOverlayService)
            
            setContent {
                VultTheme {
                    SecurityLockScreen(
                        appName = appName,
                        onUnlock = {
                            broadcastUnlock()
                            removeOverlay()
                            stopSelf()
                        }
                    )
                }
            }
        }

        overlayView = composeView
        windowManager.addView(overlayView, params)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun broadcastUnlock() {
        val intent = Intent("com.ayan.vult.ACTION_UNLOCK").apply {
            putExtra("PACKAGE_NAME", currentPackageName)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Composable
    fun SecurityLockScreen(
        appName: String,
        onUnlock: () -> Unit
    ) {
        val context = LocalContext.current
        val securityDataStore = remember { SecurityDataStore(context) }
        var inputCode by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.key == Key.Back) {
                        // EXIT TO HOME on back button
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(homeIntent)
                        removeOverlay()
                        stopSelf()
                        true
                    } else {
                        false
                    }
                },
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Vult Locked",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // PIN Entry indicators for 9 digits
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    repeat(9) { index ->
                        val char = inputCode.getOrNull(index)
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (char != null) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if (isError) {
                    Text(
                        text = "Incorrect Code",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AlphanumericKeypad(
                    onDigitClick = { digit ->
                        if (inputCode.length < 9) {
                            inputCode += digit
                            isError = false
                            if (inputCode.length == 9) {
                                coroutineScope.launch {
                                    val correctCode = securityDataStore.securityCode.first()
                                    if (inputCode == correctCode) {
                                        onUnlock()
                                    } else {
                                        isError = true
                                        inputCode = ""
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (inputCode.isNotEmpty()) {
                            inputCode = inputCode.dropLast(1)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun AlphanumericKeypad(
        onDigitClick: (String) -> Unit,
        onDeleteClick: () -> Unit
    ) {
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { key ->
                        if (key == "DEL") {
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (key.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onDigitClick(key) },
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                    }
                }
            }
        }
    }
}
