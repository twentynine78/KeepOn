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

                    if (!KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, context)) {
                        val startIntent = Intent(context, ScreenTimeoutObserverService::class.java)
                        startIntent.action = ACTION_START_FOREGROUND_TIMEOUT_SERVICE
                        ContextCompat.startForegroundService(context, startIntent)
                    }

                    if (!KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, context)
                        && KeepOnUtils.getKeepOn(context)
                    ) {
                        val startIntent = Intent(context, ScreenOffReceiverService::class.java)
                        startIntent.action = ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
                        ContextCompat.startForegroundService(context, startIntent)
                    }
                }
                ACTION_START_FOREGROUND_TIMEOUT_SERVICE -> {
                    if (!KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, context)) {
                        val startIntent = Intent(context, ScreenTimeoutObserverService::class.java)
                        startIntent.action = action
                        ContextCompat.startForegroundService(context, startIntent)
                    }
                }
                ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> {
                    if (KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, context)) {
                        val stopIntent = Intent(context, ScreenTimeoutObserverService::class.java)
                        stopIntent.action = action
                        ContextCompat.startForegroundService(context, stopIntent)
                    }
                }
                ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    if (KeepOnUtils.getTileAdded(context)) {
                        if (!KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, context)) {
                            val startIntent = Intent(context, ScreenOffReceiverService::class.java)
                            startIntent.action = action
                            ContextCompat.startForegroundService(context, startIntent)
                        }
                    }
                }
                ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> {
                    if (KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, context)) {
                        val stopIntent = Intent(context, ScreenOffReceiverService::class.java)
                        stopIntent.action = action
                        ContextCompat.startForegroundService(context, stopIntent)
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
    }
}