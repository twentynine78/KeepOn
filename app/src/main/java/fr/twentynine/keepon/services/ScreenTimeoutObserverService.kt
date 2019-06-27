package fr.twentynine.keepon.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import fr.twentynine.keepon.observer.ScreenTimeoutObserver
import fr.twentynine.keepon.receivers.ServicesRestarterReceiver
import fr.twentynine.keepon.utils.KeepOnUtils


class ScreenTimeoutObserverService : Service() {

    private var screenTimeoutObserver: ScreenTimeoutObserver? = null
    private var restart = true


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        screenTimeoutObserver = ScreenTimeoutObserver(Handler(), this)
        registerScreenTimeoutObserver(screenTimeoutObserver!!, this)
    }

    override fun onDestroy() {
        unregisterScreenTimeoutObserver(screenTimeoutObserver!!, this)

        if (restart) {
            val broadcastIntent = Intent(this, ServicesRestarterReceiver::class.java)
            broadcastIntent.action = ServicesRestarterReceiver.RESTART_SCREEN_TIMEOUT_OBSERVER_SERVICE
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
        startForeground(1, KeepOnUtils.createNotification(this))
    }

    private fun stopForegroundService() {
        restart = false

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver, context: Context) {
        val contentResolver = context.contentResolver
        val setting = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)

        contentResolver.registerContentObserver(setting, false, screenTimeoutObserver)
    }

    private fun unregisterScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver, context: Context) {
        val contentResolver = context.contentResolver

        contentResolver.unregisterContentObserver(screenTimeoutObserver)
    }

    companion object {
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }
}
