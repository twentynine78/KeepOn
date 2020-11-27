package fr.twentynine.keepon

import leakcanary.AppWatcher

@Suppress("unused")
class DebugKeepOnApplication : KeepOnApplication() {

    override fun onCreate() {
        super.onCreate()
        // Install Leak Canary on UI process
        if (isUiProcess()) {
            AppWatcher.manualInstall(this)
        }
    }

    private fun isUiProcess(): Boolean {
        return getCurrentProcessName().endsWith(":ui")
    }
}
