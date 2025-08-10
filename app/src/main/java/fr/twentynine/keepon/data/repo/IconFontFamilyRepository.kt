package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.data.local.IconFontFamily
import kotlinx.collections.immutable.toImmutableMap

object IconFontFamilyRepository {
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
