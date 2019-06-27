package fr.twentynine.keepon.observer

import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.service.quicksettings.TileService
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.utils.KeepOnUtils


class ScreenTimeoutObserver(handler: Handler, val context: Context) : ContentObserver(handler) {

    private var previousTimeout = KeepOnUtils.getCurrentTimeout(context)

    override fun onChange(selfChange: Boolean, uri: Uri) {
        super.onChange(selfChange, uri)
        processChange()
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        processChange()
    }

    private fun processChange() {
        val currentTimeout = KeepOnUtils.getCurrentTimeout(context)
        val delta = previousTimeout - currentTimeout

        if (delta != 0) {
            if (!KeepOnUtils.getValueChange(context)) {
                KeepOnUtils.setKeepOn(false, context)
                KeepOnUtils.updateOriginalTimeout(context)

                if (KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, context))
                    KeepOnUtils.stopScreenOffReceiverService(context)
            } else {
                KeepOnUtils.setValueChange(false, context)
            }

            val componentName = ComponentName(context.applicationContext, KeepOnTileService::class.java)
            TileService.requestListeningState(context, componentName)

            KeepOnUtils.sendBroadcastUpdateMainUI(context)

            previousTimeout = currentTimeout
        }
    }
}