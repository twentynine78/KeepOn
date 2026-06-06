package fr.twentynine.keepon.data.migration

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import fr.twentynine.keepon.core.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.core.system.DynamicShortcutManager
import javax.inject.Inject

interface AppVersionManager {
    suspend fun runAppMigrationIfNeeded()
}

class AppVersionManagerImpl @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    @param:ApplicationContext private val context: Context,
) : AppVersionManager {

    override suspend fun runAppMigrationIfNeeded() {
        val lastRunVersionCode = appPreferencesRepository.getLastRunVersionCode()
        val currentVersionCode = getCurrentVersionCode()

        if (lastRunVersionCode < currentVersionCode) {
            runMigrationTasks(lastRunVersionCode)
            appPreferencesRepository.setLastRunVersionCode(currentVersionCode)
        }
    }

    private fun getCurrentVersionCode(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            packageInfo.longVersionCode
        } catch (_: PackageManager.NameNotFoundException) {
            -1
        }
    }

    private fun runMigrationTasks(lastRunVersionCode: Long) {
        if (lastRunVersionCode < 20) {
            // Remove old resources used by old KeepOn versions
            DynamicShortcutManager.removeAllDynamicShortcut(context)
            PostNotificationPermissionManager.removeOldNotificationChannelKeepOnService(context)
        }
    }
}
