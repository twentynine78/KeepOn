package fr.twentynine.keepon.util.extensions

fun <T> MutableList<T>.removeUntil(target: T) {
    synchronized(this) {
        val lastIndex = this.lastIndexOf(target)

        if (lastIndex != -1) {
            // Remove elements from the beginning up to and including the target.
            this.subList(0, lastIndex + 1).clear()
        }
    }
}
