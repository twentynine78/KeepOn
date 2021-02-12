package fr.twentynine.keepon.services

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.utils.ServiceUtils
import fr.twentynine.keepon.utils.CommonUtils
import toothpick.ktp.delegate.lazy

class ScreenOffReceiverService : LifecycleService() {

    private val screenOffReceiver: ScreenOffReceiver by lazy()
    private val serviceUtils: ServiceUtils by lazy()
    private val commonUtils: CommonUtils by lazy()

    private var restart = true

    init {
        lifecycle.addObserver(MyLifeCycleObserver())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    CommonUtils.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE -> stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        restart = false

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

        var isRunning = false
    }

    inner class MyLifeCycleObserver : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            // Inject dependencies with Toothpick
            ToothpickHelper.scopedInjection(this@ScreenOffReceiverService)

            try {
                startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_screen_off_service)))
            } catch (e: Exception) {
                return
            }

            registerScreenOffReceiver()
        }

        override fun onStart(owner: LifecycleOwner) {
            isRunning = true
        }

        override fun onStop(owner: LifecycleOwner) {
            isRunning = false

            unregisterScreenOffReceiver()

            if (restart) {
                commonUtils.startScreenOffReceiverService()
            }
        }
    }
}
