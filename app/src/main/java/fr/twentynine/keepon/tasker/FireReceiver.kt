package fr.twentynine.keepon.tasker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.core.worker.SetNewScreenTimeoutWorkScheduler

class FireReceiver : BroadcastReceiver() {

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

        if (BundleScrubber.scrub(intent) ||
            null == bundle ||
            !PluginBundleManager.isBundleValid(bundle)
        ) {
            return
        }

        // Get screen timeout value from Intent bundle
        val timeoutValue = bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, -1)

        if (timeoutValue != -1) {
            // Check if the received timeout value is valid
            val screenTimeoutValue = ScreenTimeout(timeoutValue)
            val isValidScreenTimeout = (ScreenTimeoutCatalog.screenTimeouts + ScreenTimeoutCatalog.specialScreenTimeouts)
                .contains(screenTimeoutValue)

            if (isValidScreenTimeout) {
                SetNewScreenTimeoutWorkScheduler().scheduleWork(
                    timeoutValue,
                    context.applicationContext,
                    true
                )
                return
            }
        }

        Toast.makeText(
            context.applicationContext,
            R.string.toast_invalid_screen_timeout,
            Toast.LENGTH_SHORT
        ).show()
    }
}
