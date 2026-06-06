package fr.twentynine.keepon.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.domain.usecase.timeout.ResetSystemScreenTimeoutUseCase
import fr.twentynine.keepon.core.util.goAsync
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class ScreenOffReceiver : BroadcastReceiver() {

    @Inject
    lateinit var resetSystemScreenTimeoutUseCase: ResetSystemScreenTimeoutUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            goAsync(Dispatchers.Default) {
                resetSystemScreenTimeoutUseCase()
            }
        }
    }
}
