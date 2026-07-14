package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.ui.model.TaskerEventUI
import fr.twentynine.keepon.ui.state.TaskerEventEditUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Composes the Tasker event-edit UI state. The permission flows and the selected-event flow are
 * passed in as parameters because they are owned by the Activity-scoped ViewModel and cannot be
 * held by this Singleton-scoped producer.
 */
class TaskerEventEditStateProducer @Inject constructor(
    private val buildTaskerEventUiListProducer: BuildTaskerEventUiListProducer,
) {
    operator fun invoke(
        canWriteSystemSettingFlow: Flow<Boolean>,
        batteryIsNotOptimizedFlow: Flow<Boolean>,
        canPostNotificationFlow: Flow<Boolean>,
        selectedEventFlow: Flow<TaskerEventUI?>,
    ): Flow<TaskerEventEditUIState.Success> {
        return combine(
            canWriteSystemSettingFlow,
            batteryIsNotOptimizedFlow,
            canPostNotificationFlow,
            selectedEventFlow,
        ) { canWriteSystemSettings, batteryIsNotOptimized, canPostNotification, selectedEvent ->
            TaskerEventEditUIState.Success(
                canWriteSystemSettings = canWriteSystemSettings,
                canPostNotification = canPostNotification,
                batteryIsNotOptimized = batteryIsNotOptimized,
                events = buildTaskerEventUiListProducer(),
                selectedEvent = selectedEvent,
            )
        }
    }
}
