package fr.twentynine.keepon.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import fr.twentynine.keepon.R
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.KeepOnUtils


class ScreenOffReceiverService : Service() {
    companion object {
        private var instance: ScreenOffReceiverService? = null

        fun isInstanceCreated(): Boolean {
            return instance != null
        }
    }

    private var screenOffReceiver: ScreenOffReceiver? = null
    private var restart = true

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerScreenOffReceiver()
    }

    override fun onDestroy() {
        unregisterScreenOffReceiver()

        if (restart) {
            KeepOnUtils.startScreenOffReceiverService(this)
        }
        instance = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            val action = intent.action

            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE -> startForegroundService()
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> stopForegroundService()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        startForeground(1111, KeepOnUtils.buildNotification(this, getString(R.string.notification_screen_off_service)))
    }

    fun stopForegroundService() {
        restart = false

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenOffReceiver() {
        val intentFilter = IntentFilter()
        screenOffReceiver = ScreenOffReceiver()
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
}
