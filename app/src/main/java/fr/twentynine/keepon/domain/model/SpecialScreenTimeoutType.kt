package fr.twentynine.keepon.domain.model

private const val DEFAULT_SCREEN_TIMEOUT_VALUE = -42
private const val PREVIOUS_SCREEN_TIMEOUT_VALUE = -43

/**
 * The two sentinel [ScreenTimeout] values that stand for an action rather than a real duration:
 * "restore the user's default" and "go back to the previous value". Their negative [value]s can never
 * collide with a real system timeout (always positive), so a [ScreenTimeout] can carry them inline.
 */
enum class SpecialScreenTimeoutType(val value: Int) {
    DEFAULT_SCREEN_TIMEOUT_TYPE(DEFAULT_SCREEN_TIMEOUT_VALUE),
    PREVIOUS_SCREEN_TIMEOUT_TYPE(PREVIOUS_SCREEN_TIMEOUT_VALUE)
}
