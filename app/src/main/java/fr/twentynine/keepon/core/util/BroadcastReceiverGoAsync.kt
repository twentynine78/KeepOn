package fr.twentynine.keepon.core.util

import android.content.BroadcastReceiver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * Runs [block] as the asynchronous body of a broadcast receiver: holds the broadcast alive via
 * `goAsync()` and finishes the pending result once [block] completes or fails. Capped at 5 seconds
 * (the platform's hard limit for a receiver), and any failure is logged rather than crashing.
 */
fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) { withTimeout(5.seconds) { block() } }
        .invokeOnCompletion { cause ->
            if (cause != null) Log.e("BroadcastReceiverGoAsync", "Async receiver block failed", cause)
            pendingResult.finish()
        }
}
