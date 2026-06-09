package fr.twentynine.keepon.domain.model

import fr.twentynine.keepon.domain.catalog.IconFontFamily
import kotlinx.serialization.Serializable

/**
 * User-tunable typography for the generated timeout icon (font family, size, letter/line spacing,
 * bold/italic/underline and an outlined variant). Persisted as the chosen icon look and fed to the
 * icon generator. Defaults give a plain Roboto icon at the catalog's base size.
 */
@Serializable
data class TimeoutIconStyle(
    val iconStyleFontSize: Int = 0,
    val iconStyleFontHorizontalSpacing: Int = 0,
    val iconStyleFontVerticalSpacing: Int = 0,
    val iconFontFamilyName: String = IconFontFamily.Roboto.name,
    val iconStyleFontBold: Boolean = false,
    val iconStyleFontItalic: Boolean = false,
    val iconStyleFontUnderline: Boolean = false,
    val iconStyleTextOutlined: Boolean = false
)
