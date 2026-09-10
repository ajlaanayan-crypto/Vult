package com.ayan.vult.ui.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ayan.vult.data.SecurityDataStore
import com.ayan.vult.receiver.VultAdminReceiver
import com.ayan.vult.service.VultMonitoringService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val securityDataStore = remember { SecurityDataStore(context) }
    
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var isOverlayEnabled by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isDeviceAdminEnabled by remember { mutableStateOf(isDeviceAdminActive(context)) }
    
    val currentDuration by securityDataStore.unlockDuration.collectAsState(initial = 60000L)
    var showDurationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            isOverlayEnabled = Settings.canDrawOverlays(context)
            isDeviceAdminEnabled = isDeviceAdminActive(context)
            delay(2000)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Security Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SectionHeader("Permissions")
            }
            item {
                PermissionCard(
                    title = "Accessibility Service",
                    description = "Crucial: Detects when blocked apps are opened. Ensure Vult is toggled ON in Accessibility settings.",
                    isEnabled = isAccessibilityEnabled,
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    icon = Icons.Rounded.AccessibilityNew
                )
            }
            item {
                PermissionCard(
                    title = "System Overlay",
                    description = "Required to show the lock screen over other apps.",
                    isEnabled = isOverlayEnabled,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.packageName)
                        )
                        context.startActivity(intent)
                    },
                    icon = Icons.Rounded.Security
                )
            }
            item {
                PermissionCard(
                    title = "Device Administrator",
                    description = "Prevents easy uninstallation of Vult.",
                    isEnabled = isDeviceAdminEnabled,
                    onClick = {
                        val componentName = ComponentName(context, VultAdminReceiver::class.java)
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Protects Vult from being uninstalled.")
                        }
                        context.startActivity(intent)
                    },
                    icon = Icons.Rounded.AdminPanelSettings
                )
            }
            
            item {
                SectionHeader("App Behavior")
            }
            item {
                Card(
                    onClick = { showDurationDialog = true },
                    modifier = Modifier.padding(16.dp, 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    ListItem(
                        headlineContent = { Text("Unlock Duration", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { 
                            val label = when (currentDuration) {
                                0L -> "Always Lock"
                                60000L -> "1 Minute"
                                300000L -> "5 Minutes"
                                900000L -> "15 Minutes"
                                1800000L -> "30 Minutes"
                                3600000L -> "1 Hour"
                                else -> "\${currentDuration / 60000} Minutes"
                            }
                            Text("Current: $label") 
                        },
                        leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        trailingContent = { Icon(Icons.Rounded.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
            
            item {
                SectionHeader("Support")
            }
            item {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Rounded.BugReport, contentDescription = null) },
                        headlineContent = { Text("Troubleshooting") },
                        supportingContent = { Text("If blocking stops working, try disabling and re-enabling the Accessibility Service.") }
                    )
                }
            }
        }
    }

    if (showDurationDialog) {
        DurationSelectionDialog(
            currentDuration = currentDuration,
            onDismiss = { showDurationDialog = false },
            onConfirm = { newDuration ->
                scope.launch {
                    securityDataStore.updateUnlockDuration(newDuration)
                    showDurationDialog = false
                }
            }
        )
    }
}

@Composable
fun DurationSelectionDialog(
    currentDuration: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val options = listOf(
        0L to "Always Lock",
        60000L to "1 Minute",
        300000L to "5 Minutes",
        900000L to "15 Minutes",
        1800000L to "30 Minutes",
        3600000L to "1 Hour"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Unlock Duration") },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { (duration, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (duration == currentDuration),
                                onClick = { onConfirm(duration) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (duration == currentDuration),
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
    ) {
        ListItem(
            headlineContent = { 
                Text(title, fontWeight = FontWeight.SemiBold) 
            },
            supportingContent = { 
                Column {
                    Text(description)
                    Text(
                        if (isEnabled) "Status: ACTIVE" else "Status: INACTIVE",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            trailingContent = {
                Icon(
                    imageVector = if (isEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = context.packageName + "/" + VultMonitoringService::class.java.canonicalName
    val enabled = Settings.Secure.getInt(
        context.applicationContext.contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED, 0
    )
    if (enabled == 1) {
        val settingValue = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(settingValue)
            while (splitter.hasNext()) {
                val accessibilityService = splitter.next()
                if (accessibilityService.equals(service, ignoreCase = true)) {
                    return true
                }
            }
        }
    }
    return false
}

private fun isDeviceAdminActive(context: Context): Boolean {
    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(context, VultAdminReceiver::class.java)
    return devicePolicyManager.isAdminActive(componentName)
}
