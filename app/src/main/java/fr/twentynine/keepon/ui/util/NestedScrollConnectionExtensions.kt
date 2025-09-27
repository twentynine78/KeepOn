package fr.twentynine.keepon.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

operator fun NestedScrollConnection.plus(other: NestedScrollConnection): NestedScrollConnection {
    // Optimization: if one of the connections is "empty" (our internal instance),
    // there's no need to create a delegation chain.
    // Note: This identity check will only work if you ensure
    // that any "empty" connection uses `EmptyNestedScrollConnection`.
    // Compose's default behaviors might return their own internal instance
    // for an "empty" connection, so this optimization is limited.
    // The main reason to keep it is to handle cases where you explicitly pass
    // a connection you know is a no-op.

    // More general and robust case: return a new instance that delegates.
    val self = this // Left-hand connection (this)
    val argument = other // Right-hand connection (other)

    // If both are identical, no need to combine them further (though this is rare for behavior instances)
    if (self === argument) return self

    return object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val selfConsumed = self.onPreScroll(available, source)
            val remainingForArgument = available - selfConsumed
            val argumentConsumed = argument.onPreScroll(remainingForArgument, source)
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
            val argumentConsumedPost = argument.onPostScroll(
                consumed = consumed + selfConsumedPost, // The argument sees what the child AND 'self' consumed
                available = remainingForArgument,
                source = source
            )
            return selfConsumedPost + argumentConsumedPost
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val selfConsumed = self.onPreFling(available)
            val remainingForArgument = available - selfConsumed
            val argumentConsumed = argument.onPreFling(remainingForArgument)
            return selfConsumed + argumentConsumed
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val selfConsumedPost = self.onPostFling(consumed, available)
            val remainingForArgument = available - selfConsumedPost
            val argumentConsumedPost = argument.onPostFling(
                // The argument sees what the child AND 'self' consumed in post-fling
                consumed = consumed + selfConsumedPost,
                available = remainingForArgument
            )
            return selfConsumedPost + argumentConsumedPost
        }
    }
}
