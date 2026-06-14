package fr.twentynine.keepon.domain.model

import kotlinx.serialization.Serializable

/**
 * A tip the user has dismissed, identified by its stable catalog [id]. Persisted so a dismissed tip
 * stays hidden across launches.
 */
@Serializable
data class DismissedTip(
    val id: Int
)
