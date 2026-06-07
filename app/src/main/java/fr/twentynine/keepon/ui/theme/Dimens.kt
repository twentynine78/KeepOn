package fr.twentynine.keepon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Shared design tokens used across multiple screens/components (single source of truth).

/** Corner radius of the app's rounded content cards and FAB. */
val KeepOnCardCornerRadius = 24.dp

/** Shape of the app's fully-rounded content cards. */
val KeepOnCardShape = RoundedCornerShape(KeepOnCardCornerRadius)

/** Alpha applied to secondary (subtitle) text drawn over the background color. */
const val SubtitleContentAlpha = 0.8f
