package fr.twentynine.keepon.core.transition

import android.graphics.Bitmap

/**
 * Renders the frames of one icon-change transition. [prepare] does the heavy per-transition work
 * once (mask conversion, distance fields, particle clouds…) and returns a [TransitionFrames] that
 * composites the ALPHA_8 frame at a given progress. The same prepared renderer drives the QS tile,
 * the widget and the FAB.
 */
interface IconTransitionRenderer {
    fun prepare(from: Bitmap, to: Bitmap): TransitionFrames
}

/** A prepared transition: composites the frame at [progress] (0 → 1). */
fun interface TransitionFrames {
    fun frameAt(progress: Float): Bitmap
}
