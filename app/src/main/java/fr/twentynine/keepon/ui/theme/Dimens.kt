package fr.twentynine.keepon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Shared design tokens used across multiple screens/components (single source of truth).

/** Corner radius of the app's rounded content cards and FAB. */
val KeepOnCardCornerRadius = 24.dp

/** Shape of the app's fully-rounded content cards. */
val KeepOnCardShape = RoundedCornerShape(KeepOnCardCornerRadius)

/** Alpha applied to secondary (subtitle) text drawn over the background color. */
const val SUBTITLE_CONTENT_ALPHA = 0.8f

// Card header — paddings of the section header used inside every settings card.

/** Padding around the header row (below it and on both sides). */
val CardHeaderPadding = 20.dp

/** Gap between the header's leading icon and its title. */
val CardHeaderTitleSpacing = 6.dp

/** Gap between the header's title and its trailing info icon. */
val CardHeaderInfoIconSpacing = 12.dp

/** Horizontal inset of the expandable description under the header. */
val CardHeaderDescHorizontalPadding = 24.dp

/** Bottom padding of the expandable description under the header. */
val CardHeaderDescBottomPadding = 16.dp

// Style screen — shared layout rails so every card lines its content up on two constant columns.

/** Top inset of each Style card group above its header. */
val StyleCardTopPadding = 28.dp

/** Single left content rail shared by subtitles, slider labels/tracks and control rows. */
val StyleContentInset = 16.dp

/** Negative offset that cancels a passive radio/checkbox's built-in 2dp padding so its glyph lands
 *  exactly on the content rail (passive controls keep their drawn size — no 48dp touch target — so
 *  the inset is small). In the rounded list cards this gives the radio the same start margin as its
 *  top/bottom padding. */
val StyleRadioGlyphInset = 2.dp

/** Fixed leading-slot width so control labels share one column. */
val StyleControlSlotWidth = 56.dp

/** Gap between a control's leading slot and its label; sets the label column. */
val StyleControlLabelSpacing = 16.dp

/** Vertical padding of the animation-type radio rows. */
val StyleControlRowVerticalPadding = 8.dp

/** Vertical padding of a full-bleed list row (the font selection list) — matches the Home timeout
 *  rows' density (12dp around the same 38dp chip). */
val StyleListRowVerticalPadding = 12.dp

/** Vertical padding of a mid-card switch row (the outlined toggle). */
val StyleSwitchRowVerticalPadding = 12.dp

/** Vertical padding of the switch row that opens a card (Home behavior / Style transition). The
 *  full inset lives inside the row's clickable so the press ripple reaches the card edges, and the
 *  shared value keeps the two switches aligned across screens. */
val StyleTopSwitchRowVerticalPadding = 20.dp
