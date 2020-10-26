package fr.twentynine.keepon.services

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import fr.twentynine.keepon.R
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.KeepOnUtils

class ScreenOffReceiverService : Service() {
    private var screenOffReceiver: ScreenOffReceiver? = ScreenOffReceiver()
    private var restart = true

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        registerScreenOffReceiver()

        startForeground(SERVICE_ID, KeepOnUtils.buildNotification(this, getString(R.string.notification_screen_off_service)))
    }

    override fun onDestroy() {
        unregisterScreenOffReceiver()
        screenOffReceiver = null

        if (restart) {
            KeepOnUtils.startScreenOffReceiverService(this)
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
            if (BundleScrubber.scrub(intent) ||
                (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)
            ) {
                return START_NOT_STICKY
            }

            val action = intent.action

            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> stopForegroundService()
                }
            }
        }
        return START_STICKY
    }

    fun stopForegroundService() {
        restart = false

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenOffReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        try {
            registerReceiver(screenOffReceiver, intentFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterScreenOffReceiver() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val SERVICE_ID = 1111
    }
}
