package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy
import javax.inject.Singleton

@ApplicationScope
@Singleton
class ScreenOffReceiver : BroadcastReceiver() {

    private val preferences: Preferences by lazy()
    private val commonUtils: CommonUtils by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            preferences.setTimeout(preferences.getOriginalTimeout())

            commonUtils.stopScreenOffReceiverService()

            try {
                context.applicationContext.unregisterReceiver(this)
            } catch (e: IllegalArgumentException) {
                return
            }
        }
    }
}
