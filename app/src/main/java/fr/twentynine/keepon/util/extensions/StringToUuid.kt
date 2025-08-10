package fr.twentynine.keepon.util.extensions

import java.util.UUID

fun String.uuid(): UUID {
    return UUID.nameUUIDFromBytes(this.toByteArray())
}
