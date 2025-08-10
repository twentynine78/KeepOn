package fr.twentynine.keepon.util

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import javax.inject.Inject

interface AppVersionManager {
    suspend fun runAppMigrationIfNeeded()
}

class AppVersionManagerImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationContext private val context: Context,
) : AppVersionManager {

    override suspend fun runAppMigrationIfNeeded() {
        val lastRunVersionCode = userPreferencesRepository.getLastRunVersionCode()
        val currentVersionCode = getCurrentVersionCode()

        if (lastRunVersionCode < currentVersionCode) {
            runMigrationTasks(lastRunVersionCode)
            userPreferencesRepository.setLastRunVersionCode(currentVersionCode)
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
