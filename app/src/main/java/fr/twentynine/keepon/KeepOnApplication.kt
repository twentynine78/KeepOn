package fr.twentynine.keepon

import android.app.Application
import fr.twentynine.keepon.di.ToothpickHelper

@Suppress("unused")
class KeepOnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)
    }
}
