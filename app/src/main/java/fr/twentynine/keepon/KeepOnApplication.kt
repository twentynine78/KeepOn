package fr.twentynine.keepon

import android.app.Application
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.eventbus.MyEventBusIndex
import org.greenrobot.eventbus.EventBus

@Suppress("unused")
class KeepOnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)

        EventBus.builder()
            .logNoSubscriberMessages(false)
            .sendNoSubscriberEvent(false)
            .addIndex(MyEventBusIndex())
            .installDefaultEventBus()
    }
}
