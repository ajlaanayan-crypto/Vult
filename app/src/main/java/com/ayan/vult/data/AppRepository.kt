package com.ayan.vult.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AppRepository(
    private val context: Context,
    private val blockedAppDao: BlockedAppDao
) {
    private val packageManager: PackageManager = context.packageManager

    fun getInstalledApps(): Flow<List<AppInfo>> {
        return blockedAppDao.getAllBlockedApps().map { blockedApps ->
            val blockedMap = blockedApps.associateBy { it.packageName }
            
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { app ->
                    // Filter out system apps that shouldn't be blocked, or just keep user apps
                    (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || 
                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                }
                .map { app ->
                    val packageName = app.packageName
                    val appName = packageManager.getApplicationLabel(app).toString()
                    val icon = packageManager.getApplicationIcon(app)
                    val isBlocked = blockedMap[packageName]?.isBlocked ?: false
                    
                    AppInfo(packageName, appName, icon, isBlocked)
                }
                .sortedBy { it.appName }
            
            installedApps
        }
    }

    suspend fun toggleAppBlock(appInfo: AppInfo) {
        if (appInfo.isBlocked) {
            blockedAppDao.delete(BlockedApp(appInfo.packageName, appInfo.appName, true))
        } else {
            blockedAppDao.insert(BlockedApp(appInfo.packageName, appInfo.appName, true))
        }
    }
}
