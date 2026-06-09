package fr.twentynine.keepon.core.util

/**
 * Removes everything from the start of the list up to and including the last occurrence of [target]
 * (no-op if absent). Synchronized on the list, as it backs a concurrently-mutated desired-timeout
 * queue. Used to drop already-consumed entries once the system catches up to a value.
 */
fun <T> MutableList<T>.removeUntil(target: T) {
    synchronized(this) {
        val lastIndex = this.lastIndexOf(target)

        if (lastIndex != -1) {
            // Remove elements from the beginning up to and including the target.
            this.subList(0, lastIndex + 1).clear()
        }
    }
}
