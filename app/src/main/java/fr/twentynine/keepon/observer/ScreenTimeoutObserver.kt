package fr.twentynine.keepon.observer

import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.service.quicksettings.TileService
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.KeepOnUtils


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

        // Update KeepOn state
        if (KeepOnUtils.getCurrentTimeout(context) != KeepOnUtils.getOriginalTimeout(context))
            if (KeepOnUtils.getResetOnScreenOff(context))
                KeepOnUtils.startScreenOffReceiverService(context)
        else
            KeepOnUtils.stopScreenOffReceiverService(context)

        // Update QS Tile
        TileService.requestListeningState(
            context,
            ComponentName(context.applicationContext, KeepOnTileService::class.java)
        )

        // Update Main Activity
        KeepOnUtils.sendBroadcastUpdateMainUI(context)
    }
}