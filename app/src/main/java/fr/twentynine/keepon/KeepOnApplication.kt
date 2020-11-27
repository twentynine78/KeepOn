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
open class KeepOnApplication : Application(), LifecycleObserver {

    private val commonUtils: CommonUtils by lazy()

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)

        if (isServicesProcess()) {
            commonUtils.setApplicationAsStopped()
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun processInDestroyState() {
        commonUtils.setApplicationAsStopped()
    }

    private fun isServicesProcess(): Boolean {
        return getCurrentProcessName().endsWith(":services")
    }

    protected fun getCurrentProcessName(): String {
        val pid = Process.myPid()
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return ""
    }
}
