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
    private const val MIN_FRAME_COUNT = 2

    /** Total animation duration for the slider [step] (0 = default). */
    fun durationMs(step: Int): Int {
        val clamped = step.coerceIn(-DURATION_STEP_RANGE, DURATION_STEP_RANGE)
        return (DEFAULT_DURATION_MS * STEP_FACTOR.pow(clamped)).roundToInt()
    }

    /**
     * Frame count for a [durationMs] transition targeting ~60 fps, clamped to
     * [MIN_FRAME_COUNT]..[maxFrames]. A roughly constant per-frame interval avoids both wasted frames
     * on short durations and choppiness on long ones.
     */
    fun frameCount(durationMs: Int, maxFrames: Int): Int =
        (durationMs * TARGET_FPS / MILLIS_PER_SECOND).coerceIn(MIN_FRAME_COUNT, maxFrames)
}
