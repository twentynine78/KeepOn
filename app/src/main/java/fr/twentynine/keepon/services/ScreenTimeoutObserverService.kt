package fr.twentynine.keepon.services

import android.content.ContentResolver
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.LifecycleService
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.observer.ScreenTimeoutObserver
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.ServiceUtils
import toothpick.ktp.delegate.lazy

class ScreenTimeoutObserverService : LifecycleService() {

    private val mContentResolver: ContentResolver by lazy()
    private val screenTimeoutObserver: ScreenTimeoutObserver by lazy()
    private val serviceUtils: ServiceUtils by lazy()
    private val commonUtils: CommonUtils by lazy()

    private var restart = true

    override fun onCreate() {
        super.onCreate()

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        registerScreenTimeoutObserver(screenTimeoutObserver)

        startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_timeout_service)))

        isRunning = true
    }

    override fun onDestroy() {
        unregisterScreenTimeoutObserver(screenTimeoutObserver)

        isRunning = false

        if (restart) {
            commonUtils.startScreenTimeoutObserverService()
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> stopForegroundService()
                }
            }
        }

        isRunning = true

        return START_STICKY
    }

    private fun stopForegroundService() {
        restart = false
        isRunning = false

        stopForeground(true)

        stopSelf()
    }

    private fun registerScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver) {
        if (!observerRegistered) {
            val setting = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)

            mContentResolver.registerContentObserver(setting, false, screenTimeoutObserver)

            observerRegistered = true
        }
    }

    private fun unregisterScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver) {
        mContentResolver.unregisterContentObserver(screenTimeoutObserver)

        observerRegistered = false
    }

    companion object {
        private const val SERVICE_ID = 1110
        private var observerRegistered = false

        var isRunning = false
    }
}
