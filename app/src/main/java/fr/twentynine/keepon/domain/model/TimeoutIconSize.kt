package fr.twentynine.keepon.domain.model

/**
 * The two rendering scales for the generated timeout icon, in dp: [LARGE] for the in-app FAB/preview,
 * [MEDIUM] for the QS tile and widget. Part of the icon's Coil cache key.
 */
enum class TimeoutIconSize(val size: Int) {
    LARGE(40),
    MEDIUM(24)
}
