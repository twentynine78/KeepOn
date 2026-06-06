package fr.twentynine.keepon.core.util

import java.util.UUID

fun String.uuid(): UUID {
    return UUID.nameUUIDFromBytes(this.toByteArray())
}
