package fr.twentynine.keepon.core.transition

import fr.twentynine.keepon.core.transition.renderer.AffineTransitionRenderer
import fr.twentynine.keepon.core.transition.renderer.MorphTransitionRenderer
import fr.twentynine.keepon.core.transition.renderer.ParticleTransitionRenderer
import fr.twentynine.keepon.core.transition.renderer.WarpTransitionRenderer
import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.MorphTransition
import fr.twentynine.keepon.domain.model.ParticleTransition
import fr.twentynine.keepon.domain.model.WarpTransition

/**
 * Maps an [IconTransition] (pure domain data) to its Android [IconTransitionRenderer]. This is the
 * single place that dispatches on the transition kind — adding a new transition makes this `when`
 * fail to compile until it is handled.
 */
object IconTransitionRendererFactory {
    fun create(transition: IconTransition): IconTransitionRenderer = when (transition) {
        is AffineTransition -> AffineTransitionRenderer(transition)
        is MorphTransition -> MorphTransitionRenderer(transition)
        is WarpTransition -> WarpTransitionRenderer(transition)
        is ParticleTransition -> ParticleTransitionRenderer(transition)
    }
}
