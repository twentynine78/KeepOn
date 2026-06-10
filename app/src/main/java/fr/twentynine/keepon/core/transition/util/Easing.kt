package fr.twentynine.keepon.core.transition.util

/**
 * Cubic smoothstep ease-in-out (`t * t * (3 - 2t)`, for [t] in 0..1): eases away from rest at the
 * start and decelerates into the end. Shared by the player's frame pacing and the renderers'
 * per-layer envelopes so every effect moves alike.
 */
fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)
