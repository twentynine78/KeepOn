package fr.twentynine.keepon.core.tasker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.di.qualifier.ApplicationScope
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.gateway.TaskerEventNotifier
import fr.twentynine.keepon.domain.model.TaskerEventType
import fr.twentynine.keepon.tasker.EditConditionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Emits a plug-in event to the automation hosts: records it in [TaskerEventLatch] first (so a host
 * query can never race ahead of the record), then sends REQUEST_QUERY as an explicit copy to every
 * resolved host receiver (implicit broadcasts are not delivered to other apps' manifest receivers
 * since Android 8) plus one spec-conformant implicit copy for hosts with runtime-registered
 * receivers. A single delayed re-query follows once the freshness window has closed — skipped when
 * a newer occurrence superseded it — so the host sees the condition back to unsatisfied and can
 * trigger again on the next occurrence. Everything is best-effort: the Locale spec tolerates a
 * lost REQUEST_QUERY hint, so send failures are traced, never propagated.
 */
class TaskerEventNotifierImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val taskerEventLatch: TaskerEventLatch,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val tracer: DebugTracer,
) : TaskerEventNotifier {

    override fun notifyEvent(event: TaskerEventType) {
        // Recording before broadcasting is load-bearing: the host may query back immediately.
        val seq = taskerEventLatch.record(event)

        if (sendRequestQueryToHosts() == 0) {
            // No resolved host (Tasker/Locale not installed): nothing to re-arm.
            return
        }

        // The re-arm rides on the application scope, not the caller's (goAsync-capped) job. If the
        // process dies before it fires, the next occurrence's own re-arm restores the cycle.
        applicationScope.launch {
            delay(REARM_DELAY_MS)
            // A newer occurrence supersedes this re-arm: its own re-arm delivers the eventual
            // "unsatisfied", and re-querying now would re-trigger Event profiles mid-pulse.
            if (taskerEventLatch.isCurrent(event, seq)) {
                sendRequestQueryToHosts()
            }
        }
    }

    /**
     * Returns the number of explicit copies sent to the resolved host receivers, 0 on failure —
     * never throws (both call sites run on scopes where an uncaught exception kills the process,
     * and a lost hint only costs a missed query).
     */
    private fun sendRequestQueryToHosts(): Int {
        try {
            val requestQuery = Intent(TaskerIntent.ACTION_REQUEST_QUERY)
                .putExtra(TaskerIntent.EXTRA_STRING_ACTIVITY_CLASS_NAME, EditConditionActivity::class.java.name)

            val hosts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryBroadcastReceivers(requestQuery, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryBroadcastReceivers(requestQuery, 0)
            }

            var sentCount = 0
            hosts.forEach { resolveInfo ->
                resolveInfo.activityInfo?.let { receiver ->
                    context.sendBroadcast(Intent(requestQuery).setClassName(receiver.packageName, receiver.name))
                    sentCount++
                }
            }

            // The Locale spec defines REQUEST_QUERY as an implicit broadcast: one implicit copy
            // also covers a host that registered its receiver at runtime (invisible to
            // queryBroadcastReceivers). The manifest receivers reached above never see it on
            // minSdk 28+, so no host gets queried twice.
            context.sendBroadcast(requestQuery)

            tracer.trace("TaskerEvent") { "REQUEST_QUERY sent to $sentCount resolved automation host(s) + 1 implicit copy" }
            return sentCount
        } catch (e: Exception) {
            tracer.trace("TaskerEvent") { "REQUEST_QUERY send failed: $e" }
            return 0
        }
    }

    private companion object {
        // One re-query just after the freshness window: the host sees the condition unsatisfied
        // again, deactivating the profile so the next occurrence can re-trigger it.
        const val REARM_DELAY_MS = TaskerEventLatch.FRESHNESS_WINDOW_MS + 1_000L
    }
}
