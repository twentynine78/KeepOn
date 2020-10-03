package fr.twentynine.keepon.utils.preferences

import android.content.ContentResolver
import androidx.annotation.Nullable
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.CODE_BOOLEAN
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.CODE_INTEGER
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.CODE_LONG
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.CODE_STRING
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.createContentValues
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.createQueryUri
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.extractBooleanFromCursor
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.extractIntFromCursor
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.extractLongFromCursor
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.extractStringFromCursor
import fr.twentynine.keepon.utils.preferences.MultiProvider.Companion.performQuery

/**
 * Multi Preference class
 *
 *
 * - allows access to Shared Preferences across processes through a
 * Content Provider
 */
class MultiPreferences(private val mName: String, private val resolver: ContentResolver) {
    fun setString(key: String, value: String) {
        resolver.update(
            createQueryUri(mName, key, CODE_STRING),
            createContentValues(key, value),
            null,
            null
        )
    }

    @Nullable
    fun getString(key: String, defaultValue: String): String {
        return extractStringFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_STRING),
                resolver
            ),
            defaultValue
        )
    }

    fun setInt(key: String, value: Int) {
        resolver.update(
            createQueryUri(mName, key, CODE_INTEGER),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return extractIntFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_INTEGER),
                resolver
            ),
            defaultValue
        )
    }

    fun setLong(key: String, value: Long) {
        resolver.update(
            createQueryUri(mName, key, CODE_LONG),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return extractLongFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_LONG),
                resolver
            ),
            defaultValue
        )
    }

    fun setBoolean(key: String, value: Boolean) {
        resolver.update(
            createQueryUri(mName, key, CODE_BOOLEAN),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return extractBooleanFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_BOOLEAN),
                resolver
            ),
            defaultValue
        )
    }

    /* Unused functions
    fun removePreference(key: String) {
        resolver.delete(createQueryUri(mName, key, CODE_REMOVE_KEY), null, null)
    }

    fun clearPreferences() {
        resolver.delete(createQueryUri(mName, "", CODE_PREFS), null, null)
    } */
}
