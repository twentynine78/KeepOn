package fr.twentynine.keepon.data.enums

private const val DEFAULT_SCREEN_TIMEOUT_VALUE = -42
private const val PREVIOUS_SCREEN_TIMEOUT_VALUE = -43

enum class SpecialScreenTimeoutType(val value: Int) {
    DEFAULT_SCREEN_TIMEOUT_TYPE(DEFAULT_SCREEN_TIMEOUT_VALUE),
    PREVIOUS_SCREEN_TIMEOUT_TYPE(PREVIOUS_SCREEN_TIMEOUT_VALUE)
}
