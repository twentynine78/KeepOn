package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable
import fr.twentynine.keepon.data.local.IconFontFamily
import kotlinx.serialization.Serializable

@Stable
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
    val toTimeoutIconStyle: TimeoutIconStyle
        get() {
            val newFontFamilyName = when {
                this.iconStyleTypefaceSansSerif -> IconFontFamily.Roboto.name
                this.iconStyleTypefaceSerif -> IconFontFamily.Bitter.name
                this.iconStyleTypefaceMonospace -> IconFontFamily.Roboto.name
                else -> IconFontFamily.Roboto.name
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
