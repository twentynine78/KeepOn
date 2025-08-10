package fr.twentynine.keepon.data.model

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
data class ScreenTimeoutUI(
    val value: Int,
    val displayName: String,
    val isSelected: Boolean,
    val isDefault: Boolean,
    val isCurrent: Boolean,
    val isLocked: Boolean
) : Parcelable
