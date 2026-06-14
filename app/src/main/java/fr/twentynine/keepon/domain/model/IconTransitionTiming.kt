package fr.twentynine.keepon.domain.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Shared timing of the icon transition. The frame count scales with the chosen duration to stay near
 * ~60 fps ([frameCount]), capped per surface: [FRAME_COUNT] for the tile and FAB, the smaller
 * [WIDGET_FRAME_COUNT] for the widget (launchers throttle RemoteViews updates). [durationMs] maps the
 * duration slider step (0 = default, ±[DURATION_STEP_RANGE] notches) to the total duration.
 */
object IconTransitionTiming {

    /** Max frames for the tile and FAB (the actual count is the lesser of this and the 60 fps count). */
    const val FRAME_COUNT = 24

    /** Max frames for the widget — smaller, because launchers throttle/coalesce RemoteViews updates. */
    const val WIDGET_FRAME_COUNT = 12

    /** Centre (step 0) total duration in milliseconds. */
    const val DEFAULT_DURATION_MS = 240

    /** Number of notches available each side of the centre on the duration slider. */
    const val DURATION_STEP_RANGE = 4

    // Each notch scales the duration by this factor, for perceptually even steps.
    private const val STEP_FACTOR = 1.25f

    // The frame count targets this rate, so the per-frame delay stays close to 1000/60 ms.
    private const val TARGET_FPS = 60
    private const val MILLIS_PER_SECOND = 1000
    // Floor on the frame count, set to the smallest per-surface cap (the widget's): every surface then
    // renders at least this many frames even at the shortest duration, so each effect's narrow internal
    // progress windows (the warp's mid peak + cross-fade, the vortex pinch, the flip's edge) stay
    // faithfully sampled instead of degrading to a cut as the duration shrinks. Never exceeds a surface's
    // own cap. Even, so the midpoint (progress 0.5) frame — which carries those peaks — is always drawn.
    private const val MIN_FRAME_COUNT = 12

    /** Total animation duration for the slider [step] (0 = default). */
    fun durationMs(step: Int): Int {
        val clamped = step.coerceIn(-DURATION_STEP_RANGE, DURATION_STEP_RANGE)
        return (DEFAULT_DURATION_MS * STEP_FACTOR.pow(clamped)).roundToInt()
    }

    /**
     * Frame count for a [durationMs] transition targeting ~60 fps, clamped to
     * [MIN_FRAME_COUNT]..[maxFrames] and rounded down to an even number. The floor keeps every effect's
     * internal timing windows faithfully sampled whatever the chosen duration (the duration then only
     * sets the per-frame pace); the even count guarantees the progress-0.5 frame is rendered.
     */
    fun frameCount(durationMs: Int, maxFrames: Int): Int {
        val floor = minOf(MIN_FRAME_COUNT, maxFrames)
        val raw = (durationMs * TARGET_FPS / MILLIS_PER_SECOND).coerceIn(floor, maxFrames)
        return raw - (raw % 2)
    }
}
