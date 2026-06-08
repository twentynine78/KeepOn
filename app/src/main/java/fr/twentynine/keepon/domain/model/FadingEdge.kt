package fr.twentynine.keepon.domain.model

/**
 * Which edges of the icon area fade out during a transition, so sliding content appears and
 * disappears softly at the boundary instead of being cut abruptly.
 */
enum class FadingEdge {
    NONE,
    VERTICAL,
    HORIZONTAL,
    BOTH;

    val fadesVertical: Boolean get() = this == VERTICAL || this == BOTH
    val fadesHorizontal: Boolean get() = this == HORIZONTAL || this == BOTH

    companion object {
        /** Fraction of each faded edge over which content ramps to transparent (shared design token). */
        const val FADE_FRACTION = 0.3f
    }
}
