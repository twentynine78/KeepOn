package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.domain.model.TaskerEventType

/** Presentation model of a selectable Tasker plug-in event: the domain type plus its localized label. */
@Immutable
data class TaskerEventUI(
    val type: TaskerEventType,
    val displayName: String,
)
