package fr.twentynine.keepon.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.receivers.ServicesRestarterReceiver
import fr.twentynine.keepon.utils.KeepOnUtils


class ScreenOffReceiverService : Service() {

    private var screenOffReceiver: ScreenOffReceiver? = null
    private var restart = true


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        registerScreenOffReceiver()
    }

    override fun onDestroy() {
        unregisterScreenOffReceiver()

        if (restart) {
            val broadcastIntent = Intent(this, ServicesRestarterReceiver::class.java)
            broadcastIntent.action = ServicesRestarterReceiver.RESTART_SCREEN_OFF_RECEIVER_SERVICE
            sendBroadcast(broadcastIntent)
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            val action = intent.action

            if (action != null) {
                when (action) {
                    ACTION_START_FOREGROUND_SERVICE -> startForegroundService()
                    ACTION_STOP_FOREGROUND_SERVICE -> stopForegroundService()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        startForeground(2, KeepOnUtils.createNotification(this))
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

    companion object {
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }
}
