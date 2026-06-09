package fr.twentynine.keepon.domain.catalog

import kotlinx.collections.immutable.toImmutableMap

/**
 * The fonts the user can pick for the timeout icon, keyed by [IconFontFamily.name] for lookup from a
 * persisted style. Falls back to Open Sans when a stored name is unknown.
 */
object IconFontFamilyCatalog {
    val iconFontFamilies = listOf(
        IconFontFamily.Roboto,
        IconFontFamily.Bitter,
        IconFontFamily.OpenSans,
        IconFontFamily.Caudex,
        IconFontFamily.Poppins,
        IconFontFamily.Lora
    )
        .associateBy { it.name }
        .withDefault { IconFontFamily.OpenSans }
        .toImmutableMap()
}
