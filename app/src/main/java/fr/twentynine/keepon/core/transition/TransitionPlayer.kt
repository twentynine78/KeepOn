package fr.twentynine.keepon.core.transition

import android.graphics.Bitmap
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * Plays a prepared icon-change transition as a frame sequence, shared by every surface (the QS tile,
 * the widget and the FAB). It derives the frame count from [durationMs] (~60 fps, capped at the
 * surface's [maxFrames]), composites frames `1 until frameCount` with a smooth in-out progress,
 * emitting each through [emitFrame] and suspending evenly between them. The caller settles on the
 * crisp target icon afterwards (each surface pushes its settle frame differently).
 *
 * Compositing runs on [renderContext] so a surface whose collector is on the main thread (the FAB)
 * keeps the per-pixel work off it; the tile and widget already collect on a background dispatcher
 * and leave it at the default (no context switch).
 */
object TransitionPlayer {

    /**
     * Smooth in-out progress for [frame] (in `1 until frameCount`): the cubic smoothstep eases the
     * motion away from rest at the start and decelerates into the settled icon at the end, matching
     * the FAB affine path's `FastOutSlowIn` feel (an ease-out alone starts at full speed, which reads
     * as a lurch on the first frame). Shared by every composited effect so all surfaces move alike.
     */
    fun easedProgress(frame: Int, frameCount: Int): Float {
        val t = frame.toFloat() / frameCount
        return t * t * (3f - 2f * t)
    }

    @Suppress("LongParameterList")
    suspend fun play(
        transition: IconTransition,
        from: Bitmap,
        to: Bitmap,
        durationMs: Int,
        maxFrames: Int,
        renderContext: CoroutineContext = EmptyCoroutineContext,
        emitFrame: (Bitmap) -> Unit,
    ) {
        val frameCount = IconTransitionTiming.frameCount(durationMs, maxFrames)
        val frameDelayMs = (durationMs / frameCount).toLong().milliseconds
        val frames = withContext(renderContext) {
            IconTransitionRendererFactory.create(transition).prepare(from, to)
        }
        for (frame in 1 until frameCount) {
            val bitmap = withContext(renderContext) { frames.frameAt(easedProgress(frame, frameCount)) }
            emitFrame(bitmap)
            delay(frameDelayMs)
        }
    }
}
