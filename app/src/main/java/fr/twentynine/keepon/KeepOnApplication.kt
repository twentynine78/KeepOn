package fr.twentynine.keepon

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Process
import android.service.quicksettings.TileService
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

@Suppress("unused")
class KeepOnApplication : Application() {

    private val preferences: Preferences by lazy()

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)

        if (!getCurrentProcessName().contains(':')) {
            preferences.setAppILaunched(false)
            TileService.requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
        }
    }

    private fun getCurrentProcessName(): String {
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
