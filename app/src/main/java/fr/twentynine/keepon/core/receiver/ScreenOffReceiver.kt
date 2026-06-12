package fr.twentynine.keepon.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.usecase.timeout.ResetSystemScreenTimeoutUseCase
import fr.twentynine.keepon.core.util.goAsync
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Resets the system screen timeout to its default when the screen turns off, so a long/never timeout
 * the user set doesn't persist past the current session. Registered dynamically by the screen-off
 * service; the reset runs asynchronously via [goAsync].
 */
@AndroidEntryPoint
class ScreenOffReceiver : BroadcastReceiver() {

    @Inject
    lateinit var resetSystemScreenTimeoutUseCase: ResetSystemScreenTimeoutUseCase

    @Inject
    lateinit var tracer: DebugTracer

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            tracer.trace("ScreenOff") { "ACTION_SCREEN_OFF received, resetting to the default timeout" }
            goAsync(Dispatchers.Default) {
                resetSystemScreenTimeoutUseCase()
            }
        }
    }
}
