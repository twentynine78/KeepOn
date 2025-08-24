package fr.twentynine.keepon.data.local

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.CreditInfoType

@Immutable
sealed class CreditInfo(
    val name: String,
    val author: String,
    val url: String,
    val versionResId: Int? = null,
    val type: CreditInfoType
) {
    data object Coil : CreditInfo(
        name = "Coil",
        author = "Instacart team",
        url = "https://github.com/coil-kt/coil",
        versionResId = R.string.coil_version,
        type = CreditInfoType.Library
    )
    data object Roboto : CreditInfo(
        name = "Roboto",
        author = "Christian Robertson",
        url = "https://fonts.google.com/specimen/Roboto?query=roboto",
        type = CreditInfoType.Font
    )
    data object Bitter : CreditInfo(
        name = "Bitter",
        author = "Sol Matas",
        url = "https://fonts.google.com/specimen/Bitter?query=bitter",
        type = CreditInfoType.Font
    )
    data object OpenSans : CreditInfo(
        name = "Open Sans",
        author = "Steve Matteson",
        url = "https://fonts.google.com/specimen/Open+Sans?query=open+sans",
        type = CreditInfoType.Font
    )
    data object Caudex : CreditInfo(
        name = "Caudex",
        author = "Nidud",
        url = "https://fonts.google.com/specimen/Caudex?query=Caudex",
        type = CreditInfoType.Font
    )
    data object Poppins : CreditInfo(
        name = "Poppins",
        author = "Indian Type Foundry, Jonny Pinhorn ",
        url = "https://fonts.google.com/specimen/Poppins?query=Poppins",
        type = CreditInfoType.Font
    )
    data object Lora : CreditInfo(
        name = "Lora",
        author = "Cyreal",
        url = "https://fonts.google.com/specimen/Lora?query=Lora",
        type = CreditInfoType.Font
    )
}
