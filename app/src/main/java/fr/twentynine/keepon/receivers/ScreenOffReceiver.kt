package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.BundleScrubber


class ScreenOffReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent))
            return

        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            KeepOnUtils.setTimeout(KeepOnUtils.getOriginalTimeout(context), context)

            val screenOffService = context as ScreenOffReceiverService
            screenOffService.stopForegroundService()
        }
    }
}