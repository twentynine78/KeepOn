@file:Suppress("ConvertTryFinallyToUseCall")

package fr.twentynine.keepon.utils.preferences.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.annotation.Nullable
import androidx.collection.ArrayMap

/**
 * Multi Preference provider class
 */
class MultiProvider : ContentProvider() {
    companion object {
        private const val PROVIDER_NAME = "fr.twentynine.keepon.utils.preferences.provider.MultiProvider"

        /**
         * Define all Content Urls for each type, String, int, long & boolean
         */
        private const val URL_STRING = "content://$PROVIDER_NAME/string/"
        private const val URL_INT = "content://$PROVIDER_NAME/integer/"
        private const val URL_LONG = "content://$PROVIDER_NAME/long/"
        private const val URL_BOOLEAN = "content://$PROVIDER_NAME/boolean/"
        // Special URL just for clearing preferences
        private const val URL_DELETE = "content://$PROVIDER_NAME/remove/"
        private const val URL_PREFERENCES = "content://$PROVIDER_NAME/prefs/"

        internal const val CODE_STRING = 1
        internal const val CODE_INTEGER = 2
        internal const val CODE_LONG = 3
        internal const val CODE_BOOLEAN = 4
        internal const val CODE_PREFS = 5
        internal const val CODE_REMOVE_KEY = 6
        internal const val KEY = "key"
        internal const val VALUE = "value"

        /**
         * Create UriMatcher to match all requests
         */
        private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // */* = wildcard  (name or file name / key)
            this.addURI(PROVIDER_NAME, "string/*/*", CODE_STRING)
            this.addURI(PROVIDER_NAME, "integer/*/*", CODE_INTEGER)
            this.addURI(PROVIDER_NAME, "long/*/*", CODE_LONG)
            this.addURI(PROVIDER_NAME, "boolean/*/*", CODE_BOOLEAN)
            this.addURI(PROVIDER_NAME, "remove/*/", CODE_REMOVE_KEY)
            this.addURI(PROVIDER_NAME, "prefs/*/", CODE_PREFS)
        }

        fun extractStringFromCursor(cursor: Cursor?, defaultVal: String): String {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val result = cursor.getString(cursor.getColumnIndex(VALUE))
                    cursor.close()
                    return result
                }
            }
            return defaultVal
        }

        fun extractIntFromCursor(cursor: Cursor?, defaultVal: Int): Int {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val result = cursor.getInt(cursor.getColumnIndex(VALUE))
                    cursor.close()
                    return result
                }
            }
            return defaultVal
        }

        fun extractLongFromCursor(cursor: Cursor?, defaultVal: Long): Long {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val result = cursor.getLong(cursor.getColumnIndex(VALUE))
                    cursor.close()
                    return result
                }
            }
            return defaultVal
        }

        fun extractBooleanFromCursor(cursor: Cursor?, defaultVal: Boolean): Boolean {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val result = cursor.getInt(cursor.getColumnIndex(VALUE)) == 1
                    cursor.close()
                    return result
                }
            }
            return defaultVal
        }

        fun createQueryUri(prefFileName: String, key: String, prefType: Int): Uri {
            return when (prefType) {
                CODE_STRING -> Uri.parse("$URL_STRING$prefFileName/$key")
                CODE_INTEGER -> Uri.parse("$URL_INT$prefFileName/$key")
                CODE_LONG -> Uri.parse("$URL_LONG$prefFileName/$key")
                CODE_BOOLEAN -> Uri.parse("$URL_BOOLEAN$prefFileName/$key")
                CODE_REMOVE_KEY -> Uri.parse("$URL_DELETE$prefFileName/$key")
                CODE_PREFS -> Uri.parse("$URL_PREFERENCES$prefFileName/$key")
                else -> throw IllegalArgumentException("Not Supported Type : $prefType")
            }
        }

        fun <Any> createContentValues(key: String?, value: Any): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(KEY, key)
            when (value) {
                is String -> {
                    contentValues.put(VALUE, value as String)
                }
                is Int -> {
                    contentValues.put(VALUE, value as Int)
                }
                is Long -> {
                    contentValues.put(VALUE, value as Long)
                }
                is Boolean -> {
                    contentValues.put(VALUE, value as Boolean)
                }
                else -> {
                    throw IllegalArgumentException("Unsupported type ")
                }
            }
            return contentValues
        }

        @Nullable
        fun performQuery(uri: Uri, resolver: ContentResolver): Cursor? {
            return resolver.query(uri, null, null, null, null, null)
        }
    }

    /**
     * Map to hold all current Inter actors with shared preferences
     */
    private val mPreferenceMap: ArrayMap<String, PreferenceInteractor> = ArrayMap()

    override fun onCreate(): Boolean {
        return true
    }

    /**
     * Get a new Preference Interactor, or return a previously used Interactor
     *
     * @param preferenceName the name of the preference file
     * @return a new interactor, or current one in the map
     */
    private fun getPreferenceInteractor(preferenceName: String): PreferenceInteractor? {
        return if (mPreferenceMap.containsKey(preferenceName)) {
            mPreferenceMap[preferenceName]
        } else {
            val interactor = PreferenceInteractor(context!!, preferenceName)
            mPreferenceMap[preferenceName] = interactor
            interactor
        }
    }

    @Nullable
    override fun query(uri: Uri, projection: Array<String?>?, selection: String?, selectionArgs: Array<String?>?, sortOrder: String?): Cursor? {
        val interactor: PreferenceInteractor? = getPreferenceInteractor(uri.pathSegments[1])
        when (mUriMatcher.match(uri)) {
            CODE_STRING -> {
                return try {
                    val s: String = uri.pathSegments[2]
                    if (interactor!!.hasKey(s)) preferenceToCursor(interactor.getString(s)) else null
                } catch (e: Exception) {
                    null
                }
            }
            CODE_INTEGER -> {
                return try {
                    val i: String = uri.pathSegments[2]
                    if (interactor!!.hasKey(i)) preferenceToCursor(interactor.getInt(i)) else null
                } catch (e: Exception) {
                    null
                }
            }
            CODE_LONG -> {
                return try {
                    val l: String = uri.pathSegments[2]
                    if (interactor!!.hasKey(l)) preferenceToCursor(interactor.getLong(l)) else null
                } catch (e: Exception) {
                    null
                }
            }
            CODE_BOOLEAN -> {
                return try {
                    val b: String = uri.pathSegments[2]
                    if (interactor!!.hasKey(b)) preferenceToCursor(if (interactor.getBoolean(b)) 1 else 0) else null
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String?>?): Int {
        if (values != null) {
            val interactor: PreferenceInteractor? = getPreferenceInteractor(uri.pathSegments[1])
            val key = values.getAsString(KEY)
            when (mUriMatcher.match(uri)) {
                CODE_STRING -> {
                    val s = values.getAsString(VALUE)
                    interactor!!.setString(key, s)
                }
                CODE_INTEGER -> {
                    val i = values.getAsInteger(VALUE)
                    interactor!!.setInt(key, i)
                }
                CODE_LONG -> {
                    val l = values.getAsLong(VALUE)
                    interactor!!.setLong(key, l)
                }
                CODE_BOOLEAN -> {
                    val b = values.getAsBoolean(VALUE)
                    interactor!!.setBoolean(key, b)
                }
            }
        }
        return 0
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        val interactor: PreferenceInteractor? = getPreferenceInteractor(uri.pathSegments[1])
        when (mUriMatcher.match(uri)) {
            CODE_REMOVE_KEY -> interactor!!.removePref(uri.pathSegments[2])
            CODE_PREFS -> interactor!!.clearPreference()
            else -> return 0
        }
        return 0
    }

    @Nullable
    override fun getType(uri: Uri): String {
        throw UnsupportedOperationException("not supported")
    }

    @Nullable
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw UnsupportedOperationException("not supported")
    }

    /**
     * Convert a value into a cursor object using a Matrix Cursor
     *
     * @param value the value to be converted
     * @param <Any> generic object type
     * @return a Cursor object </T>
     */
    private fun <Any> preferenceToCursor(value: Any): MatrixCursor {
        val matrixCursor = MatrixCursor(arrayOf(VALUE), 1)
        val builder = matrixCursor.newRow()
        builder.add(value)
        return matrixCursor
    }
}
