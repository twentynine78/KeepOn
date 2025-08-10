package fr.twentynine.keepon.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.extensions.goAsync
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class ScreenOffReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var qsTileUpdater: QSTileUpdater

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            goAsync(Dispatchers.Default) {
                userPreferencesRepository.resetSystemScreenTimeoutToDefault { qsTileUpdater.requestUpdate() }
            }
        }
    }
}
