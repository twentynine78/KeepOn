package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable

@Stable
data class AppInfo(
    val version: String,
    val author: String,
    val sourceCodeUrl: String,
)
