package fr.twentynine.keepon.data.local

import kotlinx.serialization.json.Json

/**
 * The [Json] instance for values persisted in the preference stores: tolerant of unknown keys so a
 * value written by a different schema version (newer app, restored backup) never makes a read throw.
 */
val PreferencesJson: Json = Json { ignoreUnknownKeys = true }
