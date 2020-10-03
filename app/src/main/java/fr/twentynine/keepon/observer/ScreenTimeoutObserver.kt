package fr.twentynine.keepon.observer

import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.service.quicksettings.TileService
import fr.twentynine.keepon.glide.GlideApp
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ScreenTimeoutObserver(val context: Context) : ContentObserver(null) {
    override fun onChange(selfChange: Boolean) {
        onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        processChange()
    }

    private fun processChange() {
        if (!KeepOnUtils.getValueChange(context))
            KeepOnUtils.updateOriginalTimeout(context)
        else
            KeepOnUtils.setValueChange(false, context)

        KeepOnUtils.setPreviousTimeout(KeepOnUtils.getNewTimeout(context), context)
        KeepOnUtils.setNewTimeout(KeepOnUtils.getCurrentTimeout(context), context)

        // Manage services
        if (KeepOnUtils.getKeepOnState(context)) {
            if (KeepOnUtils.getResetOnScreenOff(context)) {
                KeepOnUtils.startScreenOffReceiverService(context)
            }
        } else {
            KeepOnUtils.stopScreenOffReceiverService(context)
        }

        // Update QS Tile
        if (KeepOnUtils.getTileAdded(context))
            TileService.requestListeningState(context, ComponentName(context.applicationContext, KeepOnTileService::class.java))

        // Update Main Activity
        KeepOnUtils.sendBroadcastUpdateMainUI(context)

        // Manage dynamics shortcut
        KeepOnUtils.manageAppShortcut(context)

        // Trim Glide memory
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            GlideApp.get(context).trimMemory(TileService.TRIM_MEMORY_RUNNING_MODERATE)
        }
    }
}