package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable

/**
 * A selectable icon-transition row on the Style screen: the catalog entry [id] (compared against the
 * persisted selection and sent back on click) paired with its already-resolved localized [label].
 */
@Immutable
data class IconTransitionOptionUI(
    val id: String,
    val label: String,
)
