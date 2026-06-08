package fr.twentynine.keepon.core.transition.util

import kotlin.random.Random

/**
 * Low-frequency value-noise field over a [gridSize]² grid, values in [-1, 1], bilinearly sampled at
 * normalised coordinates. Seeded once for a stable result across frames. Used by the warp transition.
 */
class ValueNoiseField(seed: Long, private val gridSize: Int) {

    private val stride = gridSize + 1
    private val values: FloatArray = Random(seed).let { random ->
        FloatArray(stride * stride) { random.nextFloat() * 2f - 1f }
    }

    /** Bilinearly samples the field at normalised coordinates [u], [v] (both in 0..1). */
    fun sample(u: Float, v: Float): Float {
        val gx = (u * gridSize).coerceIn(0f, gridSize.toFloat())
        val gy = (v * gridSize).coerceIn(0f, gridSize.toFloat())
        val x0 = gx.toInt().coerceAtMost(gridSize - 1)
        val y0 = gy.toInt().coerceAtMost(gridSize - 1)
        val tx = gx - x0
        val ty = gy - y0
        val top = lerp(values[y0 * stride + x0], values[y0 * stride + x0 + 1], tx)
        val bottom = lerp(values[(y0 + 1) * stride + x0], values[(y0 + 1) * stride + x0 + 1], tx)
        return lerp(top, bottom, ty)
    }

    private fun lerp(start: Float, stop: Float, t: Float): Float = start + (stop - start) * t
}
