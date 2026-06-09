package fr.twentynine.keepon.ui.catalog

import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.catalog.CreditInfo
import fr.twentynine.keepon.domain.catalog.CreditInfoType

/**
 * Localized presentation labels for the domain credit catalog. Kept in the UI layer (like
 * [IconTransitionLabelCatalog]) so the resource ids stay out of the domain; the text is resolved
 * through the StringResourceProvider where the credit sections are built.
 */
object CreditLabelCatalog {
    private val typeNameResByType: Map<CreditInfoType, Int> = mapOf(
        CreditInfoType.Library to R.string.credit_info_type_library,
        CreditInfoType.Font to R.string.credit_info_type_font,
    )

    // Only entries with a published version expose one (currently just Coil).
    private val versionResByCredit: Map<CreditInfo, Int> = mapOf(
        CreditInfo.Coil to R.string.coil_version,
    )

    fun typeNameResFor(type: CreditInfoType): Int = typeNameResByType.getValue(type)

    fun versionResFor(credit: CreditInfo): Int? = versionResByCredit[credit]
}
