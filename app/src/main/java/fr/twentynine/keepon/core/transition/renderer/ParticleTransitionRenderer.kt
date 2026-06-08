package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.ALPHA_THRESHOLD_FRACTION
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.domain.model.ParticleTransition
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Particle dissolve: the outgoing glyph disintegrates into particles that scatter and fade over the
 * first phase, while the incoming glyph reassembles from particles converging into place over the
 * last phase (they overlap in the middle). Scatter directions are seeded once for a stable burst.
 */
class ParticleTransitionRenderer(private val transition: ParticleTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val width = to.width
        val height = to.height
        val grainPx = (transition.grain * maxOf(width, height)).coerceAtLeast(MIN_GRAIN_PX)
        val scatterPx = transition.scatter * maxOf(width, height)
        val old = buildCloud(IconMask.readAlpha(IconMask.toAlphaMask(from, width, height)), width, height, grainPx, scatterPx)
        val new = buildCloud(IconMask.readAlpha(IconMask.toAlphaMask(to, width, height)), width, height, grainPx, scatterPx)
        return TransitionFrames { progress -> buildFrame(old, new, grainPx, width, height, progress) }
    }

    private fun buildFrame(
        old: ParticleCloud,
        new: ParticleCloud,
        grainPx: Float,
        width: Int,
        height: Int,
        progress: Float,
    ): Bitmap {
        val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val outProgress = (progress / PHASE).coerceIn(0f, 1f)
        drawCloud(canvas, paint, old, grainPx, travel = outProgress, alpha = 1f - outProgress)

        val inProgress = ((progress - (1f - PHASE)) / PHASE).coerceIn(0f, 1f)
        drawCloud(canvas, paint, new, grainPx, travel = 1f - inProgress, alpha = inProgress)
        return output
    }

    private fun drawCloud(canvas: Canvas, paint: Paint, cloud: ParticleCloud, grainPx: Float, travel: Float, alpha: Float) {
        if (alpha <= 0f) return
        paint.alpha = (alpha.coerceIn(0f, 1f) * ALPHA_OPAQUE).toInt()
        val half = grainPx / 2f
        for (i in cloud.homeX.indices) {
            val cx = cloud.homeX[i] + cloud.driftX[i] * travel
            val cy = cloud.homeY[i] + cloud.driftY[i] * travel
            canvas.drawRect(cx - half, cy - half, cx + half, cy + half, paint)
        }
    }

    /** Seeds one particle per filled grid cell, each with a random scatter vector. */
    private fun buildCloud(alpha: FloatArray, width: Int, height: Int, grainPx: Float, scatterPx: Float): ParticleCloud {
        val step = grainPx.roundToInt().coerceAtLeast(1)

        // First pass: count filled cells so the particle arrays can be sized exactly (no boxing).
        var count = 0
        run {
            var y = step / 2
            while (y < height) {
                var x = step / 2
                while (x < width) {
                    if (alpha[y * width + x] > ALPHA_THRESHOLD_FRACTION) count++
                    x += step
                }
                y += step
            }
        }

        val homeX = FloatArray(count)
        val homeY = FloatArray(count)
        val driftX = FloatArray(count)
        val driftY = FloatArray(count)

        // Second pass: seed one particle per filled cell. The count pass draws no randoms, so the
        // sequence here is identical to a single-pass fill (stable scatter across runs).
        val random = Random(SEED)
        var i = 0
        var y = step / 2
        while (y < height) {
            var x = step / 2
            while (x < width) {
                if (alpha[y * width + x] > ALPHA_THRESHOLD_FRACTION) {
                    val angle = random.nextFloat() * TWO_PI
                    val travel = scatterPx * (MIN_TRAVEL + (1f - MIN_TRAVEL) * random.nextFloat())
                    homeX[i] = x.toFloat()
                    homeY[i] = y.toFloat()
                    driftX[i] = cos(angle) * travel
                    driftY[i] = sin(angle) * travel
                    i++
                }
                x += step
            }
            y += step
        }
        return ParticleCloud(homeX, homeY, driftX, driftY)
    }

    /** Particle home positions plus the scatter vector each one travels along. */
    private class ParticleCloud(
        val homeX: FloatArray,
        val homeY: FloatArray,
        val driftX: FloatArray,
        val driftY: FloatArray,
    )

    private companion object {
        const val PHASE = 0.6f
        const val SEED = 0x27D4EB2FL
        const val MIN_TRAVEL = 0.4f
        const val MIN_GRAIN_PX = 2f
        val TWO_PI = (2.0 * Math.PI).toFloat()
    }
}
