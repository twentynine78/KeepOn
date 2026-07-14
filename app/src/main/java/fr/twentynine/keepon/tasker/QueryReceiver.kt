package fr.twentynine.keepon.tasker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.core.tasker.EventPluginBundleManager
import fr.twentynine.keepon.core.tasker.TaskerEventLatch
import fr.twentynine.keepon.core.tasker.TaskerIntent
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.model.TaskerEventType
import javax.inject.Inject

/**
 * Tasker/Locale plug-in "query condition" receiver: when the host evaluates the plug-in event, it
 * validates the incoming intent and bundle, then answers through the ordered-broadcast result
 * code — satisfied while the queried event has a fresh occurrence in [TaskerEventLatch],
 * unsatisfied otherwise. Everything is synchronous in-memory state: the result code must be set
 * before this method returns, so no async dispatch is possible here.
 */
@AndroidEntryPoint
class QueryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskerEventLatch: TaskerEventLatch

    @Inject
    lateinit var tracer: DebugTracer

    override fun onReceive(context: Context, intent: Intent) {
        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            return
        }

        // Check that the Intent action will be ACTION_QUERY_CONDITION
        if (TaskerIntent.ACTION_QUERY_CONDITION != intent.action) {
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (context.packageName != intent.getPackage() &&
            ComponentName(context, this.javaClass.name) != intent.component
        ) {
            return
        }

        // The Locale spec requires QUERY_CONDITION to be an ordered broadcast; without one there
        // is no way to deliver a result code.
        if (!isOrderedBroadcast) {
            return
        }

        val bundle = intent.getBundleExtra(TaskerIntent.EXTRA_BUNDLE)

        if (BundleScrubber.scrub(bundle) ||
            null == bundle ||
            !EventPluginBundleManager.isBundleValid(bundle)
        ) {
            resultCode = TaskerIntent.RESULT_CONDITION_UNKNOWN
            return
        }

        val eventType = TaskerEventType.fromId(
            bundle.getInt(EventPluginBundleManager.BUNDLE_EXTRA_EVENT_TYPE, -1)
        )

        if (eventType == null) {
            resultCode = TaskerIntent.RESULT_CONDITION_UNKNOWN
            return
        }

        val isSatisfied = taskerEventLatch.isFresh(eventType)
        tracer.trace("TaskerQuery") {
            "QUERY_CONDITION for $eventType answered ${if (isSatisfied) "SATISFIED" else "UNSATISFIED"}"
        }

        resultCode = if (isSatisfied) {
            TaskerIntent.RESULT_CONDITION_SATISFIED
        } else {
            TaskerIntent.RESULT_CONDITION_UNSATISFIED
        }
    }
}
