package fr.twentynine.keepon.data.repo

import fr.twentynine.keepon.data.local.CreditInfo
import kotlinx.collections.immutable.toImmutableMap

object CreditInfoRepository {
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
