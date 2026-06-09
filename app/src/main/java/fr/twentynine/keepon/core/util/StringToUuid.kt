package fr.twentynine.keepon.core.util

import java.util.UUID

/** Derives a stable (name-based) UUID from this string, so equal strings always map to the same id. */
fun String.uuid(): UUID {
    return UUID.nameUUIDFromBytes(this.toByteArray())
}
