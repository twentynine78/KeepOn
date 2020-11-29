package fr.twentynine.keepon.utils.preferences.provider

import android.content.ContentResolver
import androidx.annotation.Nullable
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.CODE_BOOLEAN
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.CODE_INTEGER
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.CODE_LONG
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.CODE_REMOVE_KEY
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.CODE_STRING
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.createContentValues
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.createQueryUri
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.extractBooleanFromCursor
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.extractIntFromCursor
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.extractLongFromCursor
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.extractStringFromCursor
import fr.twentynine.keepon.utils.preferences.provider.MultiProvider.Companion.performQuery
import toothpick.InjectConstructor
import javax.inject.Singleton

/**
 * Multi Preference class
 *
 *
 * - allows access to Shared Preferences across processes through a
 * Content Provider
 */
@ApplicationScope
@Singleton
@InjectConstructor
class MultiPreferences(private val resolver: ContentResolver) {

    fun setString(mName: String, key: String, value: String) {
        resolver.update(
            createQueryUri(mName, key, CODE_STRING),
            createContentValues(key, value),
            null,
            null
        )
    }

    @Nullable
    fun getString(mName: String, key: String, defaultValue: String): String {
        return extractStringFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_STRING),
                resolver
            ),
            defaultValue
        )
    }

    fun setInt(mName: String, key: String, value: Int) {
        resolver.update(
            createQueryUri(mName, key, CODE_INTEGER),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getInt(mName: String, key: String, defaultValue: Int): Int {
        return extractIntFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_INTEGER),
                resolver
            ),
            defaultValue
        )
    }

    fun setLong(mName: String, key: String, value: Long) {
        resolver.update(
            createQueryUri(mName, key, CODE_LONG),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getLong(mName: String, key: String, defaultValue: Long): Long {
        return extractLongFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_LONG),
                resolver
            ),
            defaultValue
        )
    }

    fun setBoolean(mName: String, key: String, value: Boolean) {
        resolver.update(
            createQueryUri(mName, key, CODE_BOOLEAN),
            createContentValues(key, value),
            null,
            null
        )
    }

    fun getBoolean(mName: String, key: String, defaultValue: Boolean): Boolean {
        return extractBooleanFromCursor(
            performQuery(
                createQueryUri(mName, key, CODE_BOOLEAN),
                resolver
            ),
            defaultValue
        )
    }

    fun removePreference(mName: String, key: String) {
        resolver.delete(createQueryUri(mName, key, CODE_REMOVE_KEY), null, null)
    }

    /* Unused functions
    fun clearPreferences() {
        resolver.delete(createQueryUri(mName, "", CODE_PREFS), null, null)
    } */
}
