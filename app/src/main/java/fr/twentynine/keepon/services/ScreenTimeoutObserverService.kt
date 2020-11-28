package fr.twentynine.keepon.services

import android.content.ContentResolver
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.observer.ScreenTimeoutObserver
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.ServiceUtils
import toothpick.ktp.delegate.lazy

class ScreenTimeoutObserverService : LifecycleService() {

    private val mContentResolver: ContentResolver by lazy()
    private val screenTimeoutObserver: ScreenTimeoutObserver by lazy()
    private val serviceUtils: ServiceUtils by lazy()
    private val commonUtils: CommonUtils by lazy()

    private var restart = true

    init {
        lifecycle.addObserver(MyLifeCycleObserver())
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

        var isRunning = false
    }

    inner class MyLifeCycleObserver : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            // Inject dependencies with Toothpick
            ToothpickHelper.scopedInjection(this@ScreenTimeoutObserverService)

            registerScreenTimeoutObserver(screenTimeoutObserver)

            startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_timeout_service)))
        }

        override fun onStart(owner: LifecycleOwner) {
            isRunning = true
        }

        override fun onStop(owner: LifecycleOwner) {
            isRunning = false

            unregisterScreenTimeoutObserver(screenTimeoutObserver)
            commonUtils.setApplicationAsStopped()

            if (restart) {
                commonUtils.startScreenTimeoutObserverService()
            }
        }
    }
}
