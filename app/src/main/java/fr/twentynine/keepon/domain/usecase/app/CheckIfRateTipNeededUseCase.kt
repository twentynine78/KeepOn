package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.gateway.AppRateManager
import javax.inject.Inject

/**
 * Decides whether the "rate the app" tip should be shown: the app must have been
 * launched enough times, installed long enough, and the store must be reachable.
 * [appLaunchCount] is supplied by the caller so the rule stays reactive to the
 * launch-count flow.
 */
class CheckIfRateTipNeededUseCase @Inject constructor(
    private val appRateManager: AppRateManager,
) {
    operator fun invoke(appLaunchCount: Long): Boolean {
        if (!appRateManager.canRateApp()) {
            return false
        }

        val reachedLaunchThreshold = appLaunchCount >= MIN_LAUNCH_COUNT
        val installedLongEnough =
            System.currentTimeMillis() > appRateManager.getFirstInstallTime() + MIN_INSTALL_DURATION_MS

        return reachedLaunchThreshold && installedLongEnough
    }

    companion object {
        private const val MIN_LAUNCH_COUNT = 10L
        private const val MIN_INSTALL_DURATION_MS = 3L * 24 * 60 * 60 * 1000 // 3 days
    }
}
