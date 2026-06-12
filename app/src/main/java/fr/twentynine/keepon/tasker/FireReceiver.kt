package fr.twentynine.keepon.tasker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.core.tasker.PluginBundleManager
import fr.twentynine.keepon.core.tasker.TaskerIntent
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.domain.gateway.UserNotifier
import fr.twentynine.keepon.domain.usecase.timeout.ScheduleTaskerScreenTimeoutUseCase
import javax.inject.Inject

/**
 * Tasker/Locale plug-in "fire setting" receiver: when a Tasker action runs, it validates the incoming
 * intent and bundle, then schedules the configured timeout via [ScheduleTaskerScreenTimeoutUseCase],
 * notifying through [UserNotifier] if the requested value is invalid.
 */
@AndroidEntryPoint
class FireReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleTaskerScreenTimeoutUseCase: ScheduleTaskerScreenTimeoutUseCase

    @Inject
    lateinit var userNotifier: UserNotifier

    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            return
        }

        // Check that the Intent action will be ACTION_FIRE_SETTING
        if (TaskerIntent.ACTION_FIRE_SETTING != intent.action) {
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() &&
            ComponentName(context, this.javaClass.name) != intent.component
        ) {
            return
        }

        val bundle = intent.getBundleExtra(TaskerIntent.EXTRA_BUNDLE)

        if (BundleScrubber.scrub(bundle) ||
            null == bundle ||
            !PluginBundleManager.isBundleValid(bundle)
        ) {
            return
        }

        // Get screen timeout value from Intent bundle
        val timeoutValue = bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, -1)

        if (!scheduleTaskerScreenTimeoutUseCase(timeoutValue)) {
            userNotifier.notifyInvalidScreenTimeout()
        }
    }
}
