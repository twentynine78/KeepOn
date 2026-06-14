package fr.twentynine.keepon.ui.catalog

import kotlinx.collections.immutable.toImmutableMap

/** The third-party libraries and fonts credited on the About screen, grouped by [CreditInfoType]. */
object CreditCatalog {
    val creditInfoMap = listOf(
        CreditInfo.Coil,
        CreditInfo.Roboto,
        CreditInfo.Bitter,
        CreditInfo.OpenSans,
        CreditInfo.Caudex,
        CreditInfo.Poppins,
        CreditInfo.Lora
    )
        .groupBy(
            keySelector = { it.type },
            valueTransform = { it },
        )
        .toImmutableMap()
}
