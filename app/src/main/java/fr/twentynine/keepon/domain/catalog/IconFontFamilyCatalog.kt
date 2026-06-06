package fr.twentynine.keepon.domain.catalog

import kotlinx.collections.immutable.toImmutableMap

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
