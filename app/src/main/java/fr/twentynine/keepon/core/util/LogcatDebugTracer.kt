package fr.twentynine.keepon.core.util

import android.util.Log
import fr.twentynine.keepon.domain.gateway.DebugTracer
import javax.inject.Inject

/**
 * Debug-build [DebugTracer]: writes to logcat under the shared `KeepOn.` tag prefix, so one
 * logcat filter (`tag:KeepOn.`) surfaces the whole app choreography. Never bound in release
 * (see `TracingModule`), which lets R8 drop this class entirely.
 */
class LogcatDebugTracer @Inject constructor() : DebugTracer {
    override fun trace(tag: String, message: () -> String) {
        Log.d("KeepOn.$tag", message())
    }
}
