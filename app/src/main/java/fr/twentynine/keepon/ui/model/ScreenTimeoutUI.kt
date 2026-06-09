package fr.twentynine.keepon.ui.model

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

/**
 * Presentation model of one timeout row: the raw [value], its [displayName], and the flags the row
 * renders against (selected, default, current, and locked when it exceeds the device-policy maximum).
 * Parcelable so it can ride in swipe/selection callbacks.
 */
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
