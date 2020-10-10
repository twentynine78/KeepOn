package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.services.ScreenTimeoutObserverService
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ServicesManagerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        val action = intent.action

        if (action != null) {
            when (action) {
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    if (Preferences.getTileAdded(context)) {
                        TileService.requestListeningState(context, ComponentName(context, KeepOnTileService::class.java))
                    }

                    // Start ScreenTimeoutObserverService
                    val startIntentScreenTimeout = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    startIntentScreenTimeout.action = ACTION_START_FOREGROUND_TIMEOUT_SERVICE
                    ContextCompat.startForegroundService(context.applicationContext, startIntentScreenTimeout)

                    if (Preferences.getKeepOnState(context) && Preferences.getResetTimeoutOnScreenOff(context)) {
                        // Start ScreenOffReceiverService
                        val startIntentScreenOff = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                        startIntentScreenOff.action = ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
                        ContextCompat.startForegroundService(context.applicationContext, startIntentScreenOff)
                    }

                    // Manage dynamics shortcut
                    CoroutineScope(Dispatchers.Default).launch {
                        withTimeout(60000) {
                            KeepOnUtils.manageAppShortcut(context)
                        }
                    }
                }
                ACTION_START_FOREGROUND_TIMEOUT_SERVICE -> {
                    val startIntent = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    startIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, startIntent)
                }
                ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> {
                    val stopIntent = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    stopIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, stopIntent)
                }
                ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    val startIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    startIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, startIntent)
                }
                ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    val stopIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    stopIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, stopIntent)
                }
                ACTION_SET_TIMEOUT -> {
                    if (intent.extras != null) {
                        var newTimeout = intent.getIntExtra("timeout", 0)
                        if (newTimeout != 0) {
                            if (newTimeout == -42) newTimeout = Preferences.getOriginalTimeout(context)
                            if (newTimeout == -43) newTimeout = Preferences.getPreviousValue(context)

                            Preferences.setTimeout(newTimeout, context)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE"
        const val ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE"
        const val ACTION_START_FOREGROUND_TIMEOUT_SERVICE = "ACTION_START_FOREGROUND_TIMEOUT_SERVICE"
        const val ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE = "ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE"
        const val ACTION_SET_TIMEOUT = "ACTION_SET_TIMEOUT"
    }
}
