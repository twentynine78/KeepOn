package fr.twentynine.keepon.services

import android.content.ContentResolver
import android.os.RemoteException
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
        val setting = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT)
        try {
            mContentResolver.registerContentObserver(setting, false, screenTimeoutObserver)
        } catch (e: RemoteException) {
            return
        }
    }

    private fun unregisterScreenTimeoutObserver(screenTimeoutObserver: ScreenTimeoutObserver) {
        try {
            mContentResolver.unregisterContentObserver(screenTimeoutObserver)
        } catch (e: RemoteException) {
            return
        }
    }

    companion object {
        private const val SERVICE_ID = 1110

        var isRunning = false
    }

    inner class MyLifeCycleObserver : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            // Inject dependencies with Toothpick
            ToothpickHelper.scopedInjection(this@ScreenTimeoutObserverService)

            try {
                startForeground(SERVICE_ID, serviceUtils.buildNotification(getString(R.string.notification_timeout_service)))
            } catch (e: Exception) {
                return
            }

            registerScreenTimeoutObserver(screenTimeoutObserver)
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
