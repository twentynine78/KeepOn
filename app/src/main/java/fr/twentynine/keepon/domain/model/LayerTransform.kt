package fr.twentynine.keepon.domain.model

/**
 * Transform applied to one icon layer (the outgoing or the incoming icon) during a transition.
 * Translations are fractions of the icon size; scale is a multiplier; [rotationX] is in degrees
 * (3D flip — the FAB applies it natively, the QS tile approximates it with a vertical squash).
 */
data class LayerTransform(
    val translationXFraction: Float = 0f,
    val translationYFraction: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationX: Float = 0f,
    val alpha: Float = 1f,
) {
    companion object {
        val Identity = LayerTransform()

        fun lerp(from: LayerTransform, to: LayerTransform, fraction: Float): LayerTransform {
            val t = fraction.coerceIn(0f, 1f)
            return LayerTransform(
                translationXFraction = lerp(from.translationXFraction, to.translationXFraction, t),
                translationYFraction = lerp(from.translationYFraction, to.translationYFraction, t),
                scaleX = lerp(from.scaleX, to.scaleX, t),
                scaleY = lerp(from.scaleY, to.scaleY, t),
                rotationX = lerp(from.rotationX, to.rotationX, t),
                alpha = lerp(from.alpha, to.alpha, t),
            )
        }

        private fun lerp(start: Float, stop: Float, t: Float): Float = start + (stop - start) * t
    }
}
