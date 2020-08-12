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
        if (!KeepOnUtils.getValueChange(context)) {
            KeepOnUtils.updateOriginalTimeout(context)

            KeepOnUtils.setKeepOn(false, context)
            KeepOnUtils.stopScreenOffReceiverService(context)
        } else {
            KeepOnUtils.setValueChange(false, context)
        }

        TileService.requestListeningState(
            context,
            ComponentName(context.applicationContext, KeepOnTileService::class.java)
        )

        KeepOnUtils.sendBroadcastUpdateMainUI(context)
    }
}