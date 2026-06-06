package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.gateway.AppVersionProvider
import fr.twentynine.keepon.domain.gateway.DynamicShortcutManager
import fr.twentynine.keepon.domain.gateway.NotificationChannelManager
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

/**
 * Runs one-time data/resource migrations after an app update: compares the last-run
 * version code with the current one and, when the app has been upgraded, applies the
 * migration tasks for the versions crossed, then records the new version code.
 */
class RunAppMigrationUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val appVersionProvider: AppVersionProvider,
    private val dynamicShortcutManager: DynamicShortcutManager,
    private val notificationChannelManager: NotificationChannelManager,
) {
    suspend operator fun invoke() {
        val lastRunVersionCode = appPreferencesRepository.getLastRunVersionCode()
        val currentVersionCode = appVersionProvider.getCurrentVersionCode()

        if (lastRunVersionCode < currentVersionCode) {
            runMigrationTasks(lastRunVersionCode)
            appPreferencesRepository.setLastRunVersionCode(currentVersionCode)
        }
    }

    private fun runMigrationTasks(lastRunVersionCode: Long) {
        // Resources from versions before 20 are no longer used.
        if (lastRunVersionCode < LEGACY_RESOURCES_VERSION_CODE) {
            dynamicShortcutManager.removeAllDynamicShortcuts()
            notificationChannelManager.removeLegacyKeepOnServiceChannel()
        }
    }

    private companion object {
        const val LEGACY_RESOURCES_VERSION_CODE = 20L
    }
}
