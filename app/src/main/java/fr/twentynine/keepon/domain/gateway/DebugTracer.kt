package fr.twentynine.keepon.domain.gateway

/**
 * Domain port for debug-only runtime tracing: a logcat sink in debug builds, a no-op (stripped by
 * R8) in release. Used to trace the timing-sensitive choreography between the app's surfaces
 * (tile, widget, screen-off service, workers) that is otherwise invisible at runtime.
 */
interface DebugTracer {
    /**
     * Traces one debug event under `KeepOn.[tag]`. [message] is only evaluated when tracing is
     * active, so call sites never pay the string building in release.
     */
    fun trace(tag: String, message: () -> String)
}
