package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.model.TaskerEventType
import fr.twentynine.keepon.ui.model.TaskerEventUI
import javax.inject.Inject

/**
 * Builds the presentation list of [TaskerEventUI] from the domain event types, resolving each
 * localized label. The exhaustive label mapping is the single place to extend when a new plug-in
 * event is added.
 */
class BuildTaskerEventUiListProducer @Inject constructor(
    private val stringResourceProvider: StringResourceProvider,
) {
    operator fun invoke(): List<TaskerEventUI> = TaskerEventType.entries.map { type ->
        TaskerEventUI(
            type = type,
            displayName = stringResourceProvider.getString(labelResId(type)),
        )
    }

    private fun labelResId(type: TaskerEventType): Int = when (type) {
        TaskerEventType.RESET_ON_SCREEN_OFF -> R.string.tasker_event_reset_on_screen_off
    }
}
