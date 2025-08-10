package fr.twentynine.keepon.data.local

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.R

@Immutable
sealed class IconFontFamily(
    val name: String,
    val displayName: String,
    val regularTypefaceId: Int,
    val boldTypefaceId: Int,
    val italicTypefaceId: Int,
    val boldItalicTypefaceId: Int
) {
    data object Roboto : IconFontFamily(
        name = "roboto",
        displayName = "Roboto",
        regularTypefaceId = R.font.roboto_regular,
        boldTypefaceId = R.font.roboto_bold,
        italicTypefaceId = R.font.roboto_italic,
        boldItalicTypefaceId = R.font.roboto_bolditalic,
    )
    data object Caudex : IconFontFamily(
        name = "caudex",
        displayName = "Caudex",
        regularTypefaceId = R.font.caudex_regular,
        boldTypefaceId = R.font.caudex_bold,
        italicTypefaceId = R.font.caudex_italic,
        boldItalicTypefaceId = R.font.caudex_bolditalic,
    )
    data object Bitter : IconFontFamily(
        name = "bitter",
        displayName = "Bitter",
        regularTypefaceId = R.font.bitter_regular,
        boldTypefaceId = R.font.bitter_bold,
        italicTypefaceId = R.font.bitter_italic,
        boldItalicTypefaceId = R.font.bitter_bolditalic,
    )
    data object Poppins : IconFontFamily(
        name = "poppins",
        displayName = "Poppins",
        regularTypefaceId = R.font.poppins_regular,
        boldTypefaceId = R.font.poppins_bold,
        italicTypefaceId = R.font.poppins_italic,
        boldItalicTypefaceId = R.font.poppins_bolditalic,
    )
    data object Lora : IconFontFamily(
        name = "lora",
        displayName = "Lora",
        regularTypefaceId = R.font.lora_regular,
        boldTypefaceId = R.font.lora_bold,
        italicTypefaceId = R.font.lora_italic,
        boldItalicTypefaceId = R.font.lora_bolditalic,
    )
    data object OpenSans : IconFontFamily(
        name = "opensans",
        displayName = "Open Sans",
        regularTypefaceId = R.font.opensans_regular,
        boldTypefaceId = R.font.opensans_bold,
        italicTypefaceId = R.font.opensans_italic,
        boldItalicTypefaceId = R.font.opensans_bolditalic,
    )
}
