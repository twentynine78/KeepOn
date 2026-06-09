package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable

/**
 * A credit entry on the About screen with its display text already resolved: the library/font [name],
 * [author] and [url], plus an optional published [version] (only set for entries that expose one).
 */
@Immutable
data class CreditInfoUI(
    val name: String,
    val author: String,
    val url: String,
    val version: String?,
)
