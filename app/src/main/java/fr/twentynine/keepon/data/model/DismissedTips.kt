package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class DismissedTips(
    val id: Int
)
