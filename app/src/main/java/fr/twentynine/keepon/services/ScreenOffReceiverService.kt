package fr.twentynine.keepon.services

import androidx.lifecycle.LifecycleService
import android.content.Intent
import android.content.IntentFilter
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.ServiceUtils
import fr.twentynine.keepon.utils.CommonUtils
import toothpick.ktp.delegate.lazy

class ScreenOffReceiverService : LifecycleService() {

    private val screenOffReceiver: ScreenOffReceiver by lazy()
    private val serviceUtils: ServiceUtils by lazy()
    private val commonUtils: CommonUtils by lazy()

    private var restart = true

    override fun onCreate() {
        super.onCreate()

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        registerScreenOffReceiver()

        startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_screen_off_service)))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> stopForegroundService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterScreenOffReceiver()

        if (restart) {
            commonUtils.startScreenOffReceiverService()
        }

        super.onDestroy()
    }

    private fun stopForegroundService() {
        restart = false

        unregisterScreenOffReceiver()

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenOffReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        try {
            applicationContext.registerReceiver(screenOffReceiver, intentFilter)
        } catch (e: IllegalArgumentException) {
            return
        }
    }

    private fun unregisterScreenOffReceiver() {
        try {
            applicationContext.unregisterReceiver(screenOffReceiver)
        } catch (e: IllegalArgumentException) {
            return
        }
    }

    companion object {
        private const val SERVICE_ID = 1111
    }
}
