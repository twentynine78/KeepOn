package fr.twentynine.keepon.observer

import android.database.ContentObserver
import android.net.Uri
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ServiceScope
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy
import java.util.Calendar
import java.util.TimeZone

@ServiceScope
class ScreenTimeoutObserver : ContentObserver(null) {

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
            preferences.setAppIsLaunched(true)
        } else {
            if (!preferences.getValueChange() || (Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis >= preferences.getValueChangeTime() + 3000L)) {
                preferences.setOriginalTimeout(preferences.getCurrentTimeout())
            }
            preferences.setValueChange(false)
        }

        // Store previous timeout value
        preferences.setPreviousValue(preferences.getNewValue())
        preferences.setNewValue(preferences.getCurrentTimeout())

        // Start ScreenOffReceiverService if needed
        if (preferences.getKeepOnState()) {
            if (preferences.getResetTimeoutOnScreenOff()) {
                commonUtils.startScreenOffReceiverService()
            }
        } else {
            commonUtils.stopScreenOffReceiverService()
        }

        // Update QS Tile
        commonUtils.updateQSTile()

        // Update Main Activity
        commonUtils.sendBroadcastUpdateMainUI()

        // Manage dynamics shortcut
        commonUtils.manageAppShortcuts()
    }

    companion object {
        var isRegistered = false
    }
}
