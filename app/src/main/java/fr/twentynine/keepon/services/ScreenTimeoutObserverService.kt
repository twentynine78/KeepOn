package fr.twentynine.keepon.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import fr.twentynine.keepon.R
import fr.twentynine.keepon.observer.ScreenTimeoutObserver
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.KeepOnUtils

class ScreenTimeoutObserverService : Service() {
    private var screenTimeoutObserver: ScreenTimeoutObserver? = ScreenTimeoutObserver(this)
    private var restart = true

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        registerScreenTimeoutObserver(screenTimeoutObserver!!, this)
        startForegroundService()
    }

    override fun onDestroy() {
        unregisterScreenTimeoutObserver(screenTimeoutObserver!!, this)
        screenTimeoutObserver = null

        if (restart) {
            KeepOnUtils.startScreenTimeoutObserverService(this)
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            val action = intent.action

            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_START_FOREGROUND_TIMEOUT_SERVICE -> startForegroundService()
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> stopForegroundService()
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        startForeground(1110, KeepOnUtils.buildNotification(this, getString(R.string.notification_timeout_service)))
    }

    private fun stopForegroundService() {
        restart = false

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver, context: Context) {
        if (!observerRegistered) {
            val contentResolver = context.contentResolver
            val setting = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)

            contentResolver.registerContentObserver(setting, false, screenTimeoutObserver)

            observerRegistered = true
        }
    }

    private fun unregisterScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver, context: Context) {
        val contentResolver = context.contentResolver

        contentResolver.unregisterContentObserver(screenTimeoutObserver)

        observerRegistered = false
    }

    companion object {
        private var observerRegistered = false
    }
}
