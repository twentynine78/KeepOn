package fr.twentynine.keepon.utils.glide

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeoutIconStyle(
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
)
