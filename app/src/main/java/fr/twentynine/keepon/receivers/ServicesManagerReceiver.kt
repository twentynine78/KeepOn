package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.services.ScreenTimeoutObserverService
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class ServicesManagerReceiver : BroadcastReceiver() {

    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() && ComponentName(context, this.javaClass.name) != intent.component) {
            return
        }

        val action = intent.action

        if (action != null) {
            when (action) {
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    screenOffReceiverServiceIsRunning = false
                    screenTimeoutObserverServiceIsRunning = false

                    commonUtils.updateQSTile(0)

                    // Start ScreenTimeoutObserverService
                    commonUtils.startScreenTimeoutObserverService()

                    // Start ScreenOffReceiverService if needed
                    if (preferences.getKeepOnState() && preferences.getResetTimeoutOnScreenOff()) {
                        commonUtils.startScreenOffReceiverService()
                    }

                    // Manage dynamics shortcut
                    commonUtils.createShortcut()
                }
                ACTION_START_FOREGROUND_TIMEOUT_SERVICE -> {
                    val startIntent = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    startIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, startIntent)
                    screenTimeoutObserverServiceIsRunning = true
                }
                ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> {
                    val stopIntent = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    stopIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, stopIntent)
                    screenTimeoutObserverServiceIsRunning = false
                }
                ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    val startIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    startIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, startIntent)
                    screenOffReceiverServiceIsRunning = true
                }
                ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    val stopIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    stopIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, stopIntent)
                    screenOffReceiverServiceIsRunning = false
                }
                ACTION_SET_TIMEOUT -> {
                    if (intent.extras != null) {
                        var newTimeout = intent.getIntExtra("timeout", 0)
                        if (newTimeout != 0) {
                            if (newTimeout == -42) newTimeout = preferences.getOriginalTimeout()
                            if (newTimeout == -43) newTimeout = preferences.getPreviousValue()

                            preferences.setTimeout(newTimeout)
                        }
                    }
                }
                MANAGE_SHORTCUTS -> {
                    // Manage dynamics shortcut
                    commonUtils.createShortcut()
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
        const val MANAGE_SHORTCUTS = "MANAGE_SHORTCUTS"

        var screenOffReceiverServiceIsRunning = false
        var screenTimeoutObserverServiceIsRunning = false
    }
}
