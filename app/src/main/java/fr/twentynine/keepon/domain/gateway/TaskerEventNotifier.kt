package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.TaskerEventType

/**
 * Domain port for notifying the automation hosts (Tasker/Locale) that a plug-in event just
 * occurred, so they re-query the plug-in condition. Fire-and-forget: hosts decide whether and
 * when to query back.
 */
interface TaskerEventNotifier {
    fun notifyEvent(event: TaskerEventType)
}
