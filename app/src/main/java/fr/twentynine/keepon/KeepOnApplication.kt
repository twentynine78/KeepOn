package fr.twentynine.keepon

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.utils.CommonUtils
import toothpick.ktp.delegate.lazy

@Suppress("unused")
class KeepOnApplication : Application(), LifecycleObserver {

    private val commonUtils: CommonUtils by lazy()

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)

        if (isServicesProcess()) {
            commonUtils.setApplicationAsStoped()
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun processInDestroyState() {
        commonUtils.setApplicationAsStoped()
    }

    private fun isServicesProcess(): Boolean {
        val pid = Process.myPid()
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return processInfo.processName.endsWith(":services")
            }
        }
        return false
    }
}
