package fr.twentynine.keepon.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.utils.KeepOnUtils


class ServicesRestarterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null) {
            when (action) {
                RESTART_SCREEN_TIMEOUT_OBSERVER_SERVICE ->
                    if (KeepOnUtils.getTileAdded(context)) KeepOnUtils.startScreenTimeoutObserverService(context)
                RESTART_SCREEN_OFF_RECEIVER_SERVICE ->
                    if (KeepOnUtils.getTileAdded(context)) KeepOnUtils.startScreenOffReceiverService(context)
            }
        }
    }

    companion object {
        const val RESTART_SCREEN_TIMEOUT_OBSERVER_SERVICE = "RESTART_SCREEN_TIMEOUT_OBSERVER_SERVICE"
        const val RESTART_SCREEN_OFF_RECEIVER_SERVICE = "RESTART_SCREEN_OFF_RECEIVER_SERVICE"
    }
}