package fr.twentynine.keepon.core.util

import fr.twentynine.keepon.domain.gateway.DebugTracer
import javax.inject.Inject

/** Release-build [DebugTracer]: ignores everything — the message lambdas are never evaluated. */
class NoOpDebugTracer @Inject constructor() : DebugTracer {
    override fun trace(tag: String, message: () -> String) = Unit
}
