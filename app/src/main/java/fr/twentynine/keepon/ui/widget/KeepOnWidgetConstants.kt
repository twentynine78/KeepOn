package fr.twentynine.keepon.ui.widget

const val CORNER_RADIUS_RATIO = 2
const val OUTER_BOX_PADDING_RATIO = 30
const val BORDER_SIZE_RATIO = 2
const val IMAGE_PADDING_RATIO = 7.5f
const val WIDGET_BACKGROUND_COLOR_ALPHA = 0.25f

// Max pixel dimension for the pre-rendered API < 31 rounded background bitmaps. Keeps the
// RemoteViews payload small; FillBounds upscales beyond this (solid fills tolerate it well).
const val LEGACY_BACKGROUND_MAX_PX = 384
