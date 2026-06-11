package fr.twentynine.keepon.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Chains two [NestedScrollConnection]s into one: this connection gets first claim on each scroll/fling
 * delta and the [other] sees the remainder, with their consumed amounts summed. Lets a single nested
 * scroll drive both the top app bar and the bottom bar scroll behaviors at once.
 */
operator fun NestedScrollConnection.plus(other: NestedScrollConnection): NestedScrollConnection {
    val self = this

    // Chaining a connection with itself would double-apply it; just return it unchanged.
    if (self === other) return self

    return object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val selfConsumed = self.onPreScroll(available, source)
            val remainingForArgument = available - selfConsumed
            val argumentConsumed = other.onPreScroll(remainingForArgument, source)
            return selfConsumed + argumentConsumed
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            val selfConsumedPost = self.onPostScroll(consumed, available, source)
            val remainingForArgument = available - selfConsumedPost
            // The argument sees what 'self' consumed in post-scroll added to what the child consumed
            val argumentConsumedPost = other.onPostScroll(
                consumed = consumed + selfConsumedPost, // The argument sees what the child AND 'self' consumed
                available = remainingForArgument,
                source = source
            )
            return selfConsumedPost + argumentConsumedPost
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val selfConsumed = self.onPreFling(available)
            val remainingForArgument = available - selfConsumed
            val argumentConsumed = other.onPreFling(remainingForArgument)
            return selfConsumed + argumentConsumed
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val selfConsumedPost = self.onPostFling(consumed, available)
            val remainingForArgument = available - selfConsumedPost
            val argumentConsumedPost = other.onPostFling(
                // The argument sees what the child AND 'self' consumed in post-fling
                consumed = consumed + selfConsumedPost,
                available = remainingForArgument
            )
            return selfConsumedPost + argumentConsumedPost
        }
    }
}
