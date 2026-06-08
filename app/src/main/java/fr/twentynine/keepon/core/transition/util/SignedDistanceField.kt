package fr.twentynine.keepon.core.transition.util

import android.graphics.Bitmap
import kotlin.math.sqrt

/**
 * Signed distance field of a glyph mask: positive outside the glyph, negative inside, ~0 on the
 * contour. Computed as the difference of two exact Euclidean distance transforms (Felzenszwalb &
 * Huttenlocher, O(width·height)). Used by the silhouette-driven transitions.
 */
object SignedDistanceField {

    // Parabolas anchored "infinitely high" outside the set: large enough to dominate any in-image
    // distance yet safe from float overflow.
    private const val DISTANCE_INF = 1e20f

    fun of(mask: Bitmap): FloatArray {
        val width = mask.width
        val height = mask.height
        val alpha = IconMask.readAlpha(mask)
        val inside = BooleanArray(width * height) { alpha[it] > ALPHA_THRESHOLD_FRACTION }

        val distanceOutside = euclideanDistanceTransform(inside, width, height)
        val distanceInside = euclideanDistanceTransform(inside, width, height, invert = true)
        return FloatArray(width * height) { sqrt(distanceOutside[it]) - sqrt(distanceInside[it]) }
    }

    private fun euclideanDistanceTransform(
        set: BooleanArray,
        width: Int,
        height: Int,
        invert: Boolean = false,
    ): FloatArray {
        val grid = FloatArray(width * height) { if (set[it] != invert) 0f else DISTANCE_INF }
        val span = maxOf(width, height)
        val source = FloatArray(span)
        val distance = FloatArray(span)
        val parabola = IntArray(span)
        val boundary = FloatArray(span + 1)

        for (x in 0 until width) {
            for (y in 0 until height) source[y] = grid[y * width + x]
            transform1d(source, distance, parabola, boundary, height)
            for (y in 0 until height) grid[y * width + x] = distance[y]
        }
        for (y in 0 until height) {
            val rowBase = y * width
            for (x in 0 until width) source[x] = grid[rowBase + x]
            transform1d(source, distance, parabola, boundary, width)
            for (x in 0 until width) grid[rowBase + x] = distance[x]
        }
        return grid
    }

    private fun transform1d(
        source: FloatArray,
        distance: FloatArray,
        parabola: IntArray,
        boundary: FloatArray,
        n: Int,
    ) {
        var k = 0
        parabola[0] = 0
        boundary[0] = -DISTANCE_INF
        boundary[1] = DISTANCE_INF
        for (q in 1 until n) {
            var s = intersection(source, q, parabola[k])
            while (s <= boundary[k]) {
                k--
                s = intersection(source, q, parabola[k])
            }
            k++
            parabola[k] = q
            boundary[k] = s
            boundary[k + 1] = DISTANCE_INF
        }
        k = 0
        for (q in 0 until n) {
            while (boundary[k + 1] < q) k++
            val dx = (q - parabola[k]).toFloat()
            distance[q] = dx * dx + source[parabola[k]]
        }
    }

    private fun intersection(source: FloatArray, q: Int, p: Int): Float =
        ((source[q] + q * q) - (source[p] + p * p)) / (2f * q - 2f * p)
}
