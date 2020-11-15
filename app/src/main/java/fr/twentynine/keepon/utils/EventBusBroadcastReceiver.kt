package fr.twentynine.keepon.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.twentynine.keepon.ui.MainActivity
import fr.twentynine.keepon.ui.MainActivity.Companion.ACTION_MISSING_SETTINGS
import fr.twentynine.keepon.ui.MainActivity.Companion.ACTION_UPDATE_UI
import org.greenrobot.eventbus.EventBus

class EventBusBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    ACTION_UPDATE_UI -> {
                        EventBus.getDefault().post(MainActivity.UpdateUIEvent())
                    }
                    ACTION_MISSING_SETTINGS -> {
                        EventBus.getDefault().post(MainActivity.MissingSettingsEvent())
                    }
                }
                intent.action = null
            }
        }
}
