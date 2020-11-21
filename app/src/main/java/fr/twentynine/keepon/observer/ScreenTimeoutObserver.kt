package fr.twentynine.keepon.observer

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Singleton

@ApplicationScope
@Singleton
@InjectConstructor
class ScreenTimeoutObserver(val application: Application) : ContentObserver(null) {

    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        onChange(true)
    }

    override fun onChange(selfChange: Boolean) {
        onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        processChange(selfChange)
    }

    private fun processChange(selfChange: Boolean) {
        if (selfChange) {
            preferences.setAppILaunched(true)
        } else {
            if (!preferences.getValueChange() || (Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis >= preferences.getValueChangeTime() + 3000L)) {
                preferences.setOriginalTimeout(preferences.getCurrentTimeout())
            }
            preferences.setValueChange(false)
        }

        // Store previous timeout value
        preferences.setPreviousValue(preferences.getNewValue())
        preferences.setNewValue(preferences.getCurrentTimeout())

        // Manage services
        if (preferences.getKeepOnState()) {
            if (preferences.getResetTimeoutOnScreenOff()) {
                commonUtils.startScreenOffReceiverService()
            }
        } else {
            commonUtils.stopScreenOffReceiverService()
        }

        // Update QS Tile
        commonUtils.updateQSTile(0)

        // Update Main Activity
        commonUtils.sendBroadcastUpdateMainUI()

        // Manage dynamics shortcut
        commonUtils.manageAppShortcut()
    }

    companion object {
        var isRegistered = false
    }
}
