package fr.twentynine.keepon.data.catalog

import fr.twentynine.keepon.data.catalog.CreditInfo
import kotlinx.collections.immutable.toImmutableMap

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
