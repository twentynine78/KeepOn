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
 * last phase (they overlap in the middle). Each particle scatters along a seeded direction with a
 * per-particle start delay, so the cloud disperses and re-condenses raggedly rather than as one
 * synchronised sheet; it shrinks toward nothing as it scatters (and grows back as it converges) and
 * bows along a shared curl, so the motion reads as sublimation rather than a translate-and-fade.
 */
class ParticleTransitionRenderer(private val transition: ParticleTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val width = to.width
        val height = to.height
        val grainPx = (transition.grain * maxOf(width, height)).coerceAtLeast(MIN_GRAIN_PX)
        val scatterPx = transition.scatter * maxOf(width, height)
        val old = buildCloud(IconMask.readAlpha(IconMask.toAlphaMask(from, width, height)), width, height, grainPx, scatterPx)
        val new = buildCloud(IconMask.readAlpha(IconMask.toAlphaMask(to, width, height)), width, height, grainPx, scatterPx)
        // Reused across this playback's frames (a prepared instance is never composited concurrently).
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        return TransitionFrames { progress -> buildFrame(old, new, grainPx, width, height, paint, progress) }
    }

    @Suppress("LongParameterList")
    private fun buildFrame(
        old: ParticleCloud,
        new: ParticleCloud,
        grainPx: Float,
        width: Int,
        height: Int,
        paint: Paint,
        progress: Float,
    ): Bitmap {
        val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(output)

        val outPhase = (progress / PHASE).coerceIn(0f, 1f)
        drawCloud(canvas, paint, old, grainPx, phase = outPhase, entering = false)

        val inPhase = ((progress - (1f - PHASE)) / PHASE).coerceIn(0f, 1f)
        drawCloud(canvas, paint, new, grainPx, phase = inPhase, entering = true)
        return output
    }

    /**
     * Draws one cloud at its [phase] (0..1 within its half of the transition). Each particle runs over
     * a staggered window `[delay, delay + 1 - MAX_STAGGER]`, so they leave/arrive at different times.
     * [dispersal] is how far a particle is from home (0 = settled, 1 = fully scattered): the outgoing
     * cloud disperses as the phase advances, the incoming one converges. A scattered particle is faint,
     * shrunken and bowed along a shared curl; a settled one is full size and opacity (matching the
     * crisp icon the surface settles on).
     */
    private fun drawCloud(canvas: Canvas, paint: Paint, cloud: ParticleCloud, grainPx: Float, phase: Float, entering: Boolean) {
        val fullHalf = grainPx / 2f
        for (i in cloud.homeX.indices) {
            val local = ((phase - cloud.delay[i]) / (1f - MAX_STAGGER)).coerceIn(0f, 1f)
            val dispersal = if (entering) 1f - local else local
            val alpha = if (entering) local else 1f - local
            val half = fullHalf * (1f - dispersal)
            if (alpha <= 0f || half <= 0f) continue

            // Bow the straight scatter into an arc that peaks mid-flight (0 at both ends) by offsetting
            // along the perpendicular (-driftY, driftX), so the whole cloud swirls coherently.
            val arc = CURL * dispersal * (1f - dispersal) * ARC_PEAK
            val cx = cloud.homeX[i] + cloud.driftX[i] * dispersal - cloud.driftY[i] * arc
            val cy = cloud.homeY[i] + cloud.driftY[i] * dispersal + cloud.driftX[i] * arc
            paint.alpha = (alpha * ALPHA_OPAQUE).toInt()
            canvas.drawRect(cx - half, cy - half, cx + half, cy + half, paint)
        }
    }

    /** Seeds one particle per filled grid cell, each with a random scatter vector and start delay. */
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
        val delay = FloatArray(count)

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
                    val distance = scatterPx * (MIN_TRAVEL + (1f - MIN_TRAVEL) * random.nextFloat())
                    homeX[i] = x.toFloat()
                    homeY[i] = y.toFloat()
                    driftX[i] = cos(angle) * distance
                    driftY[i] = sin(angle) * distance
                    delay[i] = random.nextFloat() * MAX_STAGGER
                    i++
                }
                x += step
            }
            y += step
        }
        return ParticleCloud(homeX, homeY, driftX, driftY, delay)
    }

    /** Particle home positions, the scatter vector each travels along, and its staggered start delay. */
    private class ParticleCloud(
        val homeX: FloatArray,
        val homeY: FloatArray,
        val driftX: FloatArray,
        val driftY: FloatArray,
        val delay: FloatArray,
    )

    private companion object {
        const val PHASE = 0.7f
        const val SEED = 0x27D4EB2FL
        const val MIN_TRAVEL = 0.4f
        const val MIN_GRAIN_PX = 2f

        // Latest fraction of a phase a particle may wait before it starts moving (the rest of the
        // phase is its travel window), so the cloud disperses/condenses raggedly instead of in lockstep.
        const val MAX_STAGGER = 0.35f

        // Perpendicular bow as a fraction of the scatter vector, peaking mid-flight, for a coherent swirl.
        const val CURL = 0.22f
        const val ARC_PEAK = 4f

        const val TWO_PI = (2.0 * Math.PI).toFloat()
    }
}
