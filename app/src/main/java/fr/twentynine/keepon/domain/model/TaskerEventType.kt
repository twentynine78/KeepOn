package fr.twentynine.keepon.domain.model

/**
 * The automation-plug-in (Tasker/Locale) events KeepOn can emit. [id] is persisted inside users'
 * saved Tasker profile bundles — never renumber, reuse, or remove ids.
 */
enum class TaskerEventType(val id: Int) {
    RESET_ON_SCREEN_OFF(1);

    companion object {
        fun fromId(id: Int): TaskerEventType? = entries.firstOrNull { it.id == id }
    }
}
