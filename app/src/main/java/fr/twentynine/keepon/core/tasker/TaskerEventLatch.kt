package fr.twentynine.keepon.core.tasker

import android.os.SystemClock
import fr.twentynine.keepon.domain.model.TaskerEventType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide record of the last occurrence of each plug-in event. The event notifier records an
 * occurrence just before asking the hosts to re-query, and the query receiver answers "satisfied"
 * while that occurrence is fresh. Reads are non-consuming because the host sends one query per
 * profile using the event. Each occurrence also carries a sequence number so a delayed re-arm can
 * detect it was superseded by a newer occurrence (and skip its re-query, which would otherwise
 * re-trigger Event profiles mid-pulse).
 */
@Singleton
class TaskerEventLatch @Inject constructor() {

    private val lastOccurrence = ConcurrentHashMap<TaskerEventType, Occurrence>()
    private val sequence = AtomicLong(0)

    /**
     * Records that [event] just occurred and returns its sequence number. Must be called BEFORE
     * broadcasting the re-query hint.
     */
    fun record(event: TaskerEventType): Long {
        val seq = sequence.incrementAndGet()
        lastOccurrence[event] = Occurrence(seq, SystemClock.elapsedRealtime())
        return seq
    }

    /** True while [event]'s last occurrence is within [FRESHNESS_WINDOW_MS]. */
    fun isFresh(event: TaskerEventType): Boolean {
        val occurrence = lastOccurrence[event] ?: return false
        return SystemClock.elapsedRealtime() - occurrence.atElapsedMs <= FRESHNESS_WINDOW_MS
    }

    /** True while [seq] is still the latest recorded occurrence of [event]. */
    fun isCurrent(event: TaskerEventType, seq: Long): Boolean {
        return lastOccurrence[event]?.seq == seq
    }

    private data class Occurrence(val seq: Long, val atElapsedMs: Long)

    companion object {
        /** How long an occurrence keeps answering "satisfied" to host queries. */
        const val FRESHNESS_WINDOW_MS = 5_000L
    }
}
