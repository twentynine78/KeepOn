package fr.twentynine.keepon.services

import androidx.lifecycle.LifecycleService
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.receivers.ScreenOffReceiver
import fr.twentynine.keepon.utils.ServiceUtils
import fr.twentynine.keepon.utils.CommonUtils
import toothpick.ktp.delegate.lazy

class ScreenOffReceiverService : LifecycleService(), LifecycleObserver {

    private val screenOffReceiver: ScreenOffReceiver by lazy()
    private val serviceUtils: ServiceUtils by lazy()
    private val commonUtils: CommonUtils by lazy()

    private var restart = true

    init {
        lifecycle.addObserver(this)
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

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun serviceInCreatedState() {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        registerScreenOffReceiver()

        startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_screen_off_service)))
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun serviceInStartedState() {
        isRunning = true
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun serviceInStoppedState() {
        isRunning = false

        unregisterScreenOffReceiver()

        if (restart) {
            commonUtils.startScreenOffReceiverService()
        }
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
}
