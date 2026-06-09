package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable

/**
 * A grouped section of the About screen credits: the already-resolved [typeName] heading (e.g.
 * "Library", "Font") and the [credits] listed under it.
 */
@Immutable
data class CreditSectionUI(
    val typeName: String,
    val credits: List<CreditInfoUI>,
)
