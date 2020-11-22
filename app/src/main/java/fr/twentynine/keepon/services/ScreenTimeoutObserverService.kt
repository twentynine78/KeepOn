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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    ServicesManagerReceiver.ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE -> stopForegroundService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopService()

        if (restart) {
            commonUtils.startScreenTimeoutObserverService()
        }

        super.onDestroy()
    }

    private fun stopForegroundService() {
        restart = false

        stopService()

        stopForeground(true)

        stopSelf()
    }

    private fun stopService() {
        unregisterScreenTimeoutObserver(screenTimeoutObserver)
        commonUtils.setApplicationAsStoped()
    }

    private fun registerScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver) {
        if (!ScreenTimeoutObserver.isRegistered) {
            ScreenTimeoutObserver.isRegistered = true

            val setting = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)
            mContentResolver.registerContentObserver(setting, false, screenTimeoutObserver)
        }
    }

    private fun unregisterScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver) {
        ScreenTimeoutObserver.isRegistered = false

        mContentResolver.unregisterContentObserver(screenTimeoutObserver)
    }

    companion object {
        private const val SERVICE_ID = 1110
    }
}
