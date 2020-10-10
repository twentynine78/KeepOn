package fr.twentynine.keepon.observer

import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.service.quicksettings.TileService
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ScreenTimeoutObserver(val context: Context) : ContentObserver(null) {
    override fun onChange(selfChange: Boolean) {
        onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        processChange()
    }

    private fun processChange() {
        if (!Preferences.getValueChange(context)) {
            Preferences.setOriginalTimeout(Preferences.getCurrentTimeout(context), context)
        } else {
            Preferences.setValueChange(false, context)
        }

        Preferences.setPreviousValue(Preferences.getNewValue(context), context)
        Preferences.setNewValue(Preferences.getCurrentTimeout(context), context)

        // Manage services
        if (Preferences.getKeepOnState(context)) {
            if (Preferences.getResetTimeoutOnScreenOff(context)) {
                KeepOnUtils.startScreenOffReceiverService(context)
            }
        } else {
            KeepOnUtils.stopScreenOffReceiverService(context)
        }

        // Update QS Tile
        if (Preferences.getTileAdded(context)) {
            TileService.requestListeningState(context, ComponentName(context.applicationContext, KeepOnTileService::class.java))
        }

        // Update Main Activity
        KeepOnUtils.sendBroadcastUpdateMainUI(context)

        // Manage dynamics shortcut
        CoroutineScope(Dispatchers.Default).launch {
            withTimeout(60000) {
                KeepOnUtils.manageAppShortcut(context)
            }
        }
    }
}
