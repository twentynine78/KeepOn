package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.utils.KeepOnUtils


class ScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            KeepOnUtils.setTimeout(KeepOnUtils.getOriginalTimeout(context), context)

            val screenOffService = context as ScreenOffReceiverService
            screenOffService.stopForegroundService()
        }
    }
}