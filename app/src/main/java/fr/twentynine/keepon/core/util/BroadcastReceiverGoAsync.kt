package fr.twentynine.keepon.core.util

import android.content.BroadcastReceiver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * Runs [block] as the asynchronous body of a broadcast receiver: holds the broadcast alive via
 * `goAsync()` and finishes the pending result once [block] completes or fails. Capped at 5 seconds
 * (the platform's hard limit for a receiver). Failures are caught INSIDE the coroutine and logged:
 * an exception left uncaught in a root GlobalScope coroutine reaches the process's default handler
 * and crashes the app (a completion observer cannot consume it).
 */
fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            withTimeout(5.seconds) { block() }
        } catch (timeout: TimeoutCancellationException) {
            Log.e("BroadcastReceiverGoAsync", "Async receiver block timed out", timeout)
        } catch (failure: Throwable) {
            Log.e("BroadcastReceiverGoAsync", "Async receiver block failed", failure)
        } finally {
            pendingResult.finish()
        }
    }
}
