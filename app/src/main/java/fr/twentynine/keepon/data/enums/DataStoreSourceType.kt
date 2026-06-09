package fr.twentynine.keepon.data.enums

/**
 * Selects which DataStore a preference lives in: [DATA_SOURCE_BACKED_UP] is included in Android's
 * auto-backup (user settings that should survive a reinstall), [DATA_SOURCE] is device-local state
 * that must not be restored onto a different install.
 */
enum class DataStoreSourceType {
    DATA_SOURCE_BACKED_UP,
    DATA_SOURCE
}
