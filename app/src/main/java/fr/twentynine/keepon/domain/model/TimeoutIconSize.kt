package fr.twentynine.keepon.domain.model

/**
 * The two rendering scales for the generated timeout icon, in dp: [LARGE] for the in-app FAB and the
 * home-screen widget, [MEDIUM] for the QS tile, the list chips and the transition-grid previews.
 * Part of the icon's Coil cache key.
 */
enum class TimeoutIconSize(val size: Int) {
    LARGE(40),
    MEDIUM(24)
}
