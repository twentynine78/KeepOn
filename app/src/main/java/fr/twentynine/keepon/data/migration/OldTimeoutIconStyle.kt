package fr.twentynine.keepon.data.migration

import fr.twentynine.keepon.domain.catalog.IconFontFamily
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import kotlinx.serialization.Serializable

/**
 * The legacy persisted icon-style model from earlier app versions. Still read by the migrating
 * UI-preferences repository when only the old key is present; [toTimeoutIconStyle] maps it onto the
 * current [TimeoutIconStyle] (collapsing the old typeface booleans to a font family, dropping the
 * fill/stroke triad to the single outlined flag, etc.).
 */
@Serializable
data class OldTimeoutIconStyle(
    val iconStyleFontSize: Int = 0,
    val iconStyleFontSkew: Int = 0,
    val iconStyleFontSpacing: Int = 0,
    val iconStyleTypefaceSansSerif: Boolean = true,
    val iconStyleTypefaceSerif: Boolean = false,
    val iconStyleTypefaceMonospace: Boolean = false,
    val iconStyleFontBold: Boolean = true,
    val iconStyleFontUnderline: Boolean = false,
    val iconStyleFontSMCP: Boolean = false,
    val iconStyleTextFill: Boolean = true,
    val iconStyleTextFillStroke: Boolean = false,
    val iconStyleTextStroke: Boolean = false
) {
    /** Maps this legacy style onto the current [TimeoutIconStyle]. */
    val toTimeoutIconStyle: TimeoutIconStyle
        get() {
            // Monospace was dropped from the catalog, so only serif keeps a distinct family;
            // sans-serif, monospace and the unset case all land on Roboto.
            val newFontFamilyName = if (this.iconStyleTypefaceSerif) {
                IconFontFamily.Bitter.name
            } else {
                IconFontFamily.Roboto.name
            }

            return TimeoutIconStyle(
                iconStyleFontSize = if (this.iconStyleFontSize != 0) this.iconStyleFontSize - 1 else 0,
                iconStyleFontHorizontalSpacing = this.iconStyleFontSpacing,
                iconStyleFontVerticalSpacing = 0,
                iconFontFamilyName = newFontFamilyName,
                iconStyleFontBold = this.iconStyleFontBold || this.iconStyleTextFillStroke,
                iconStyleFontItalic = this.iconStyleFontSkew == 1,
                iconStyleFontUnderline = this.iconStyleFontUnderline,
                iconStyleTextOutlined = this.iconStyleTextStroke,
            )
        }
}
