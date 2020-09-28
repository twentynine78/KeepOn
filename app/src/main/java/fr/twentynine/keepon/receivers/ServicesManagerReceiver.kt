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
import fr.twentynine.keepon.utils.KeepOnUtils


class ServicesManagerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null) {
            when (action) {
                Intent.ACTION_BOOT_COMPLETED -> {
                    TileService.requestListeningState(context, ComponentName(context, KeepOnTileService::class.java))

                    val startIntentScreenTimeout = Intent(context.applicationContext, ScreenTimeoutObserverService::class.java)
                    startIntentScreenTimeout.action = ACTION_START_FOREGROUND_TIMEOUT_SERVICE
                    ContextCompat.startForegroundService(context.applicationContext, startIntentScreenTimeout)

                    if (KeepOnUtils.getKeepOn(context)) {
                        val startIntentScreenOff = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                        startIntentScreenOff.action = ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
                        ContextCompat.startForegroundService(context.applicationContext, startIntentScreenOff)
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
                    if (KeepOnUtils.getTileAdded(context)) {
                        val startIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                        startIntent.action = action
                        ContextCompat.startForegroundService(context.applicationContext, startIntent)
                    }
                }
                ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    val stopIntent = Intent(context.applicationContext, ScreenOffReceiverService::class.java)
                    stopIntent.action = action
                    ContextCompat.startForegroundService(context.applicationContext, stopIntent)
                }
                ACTION_SET_TIMEOUT -> {
                    var newTimeout = intent.getIntExtra("timeout", 0)
                    if (newTimeout != 0) {
                        if (newTimeout == -1) newTimeout = KeepOnUtils.getOriginalTimeout(context)

                        KeepOnUtils.setNewTimeout(newTimeout, context)

                        if (newTimeout == KeepOnUtils.getOriginalTimeout(context)) {
                            KeepOnUtils.setKeepOn(false, context)
                            KeepOnUtils.stopScreenOffReceiverService(context)
                        } else {
                            KeepOnUtils.setKeepOn(true, context)
                            if (KeepOnUtils.getResetOnScreenOff(context))
                                KeepOnUtils.startScreenOffReceiverService(context)
                        }

                        KeepOnUtils.setTimeout(newTimeout, context)
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