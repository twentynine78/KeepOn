package fr.twentynine.keepon.utils.preferences

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
        private const val PROVIDER_NAME = "fr.twentynine.keepon.utils.preferences.MultiProvider"

        /**
         * Define all Content Urls for each type, String, int, long & boolean
         */
        private const val URL_STRING =
            "content://$PROVIDER_NAME/string/"
        private const val URL_INT =
            "content://$PROVIDER_NAME/integer/"
        private const val URL_LONG =
            "content://$PROVIDER_NAME/long/"
        private const val URL_BOOLEAN =
            "content://$PROVIDER_NAME/boolean/"

        // Special URL just for clearing preferences
        private const val URL_PREFERENCES =
            "content://$PROVIDER_NAME/prefs/"
        const val CODE_STRING = 1
        const val CODE_INTEGER = 2
        const val CODE_LONG = 3
        const val CODE_BOOLEAN = 4
        const val CODE_PREFS = 5
        const val CODE_REMOVE_KEY = 6
        const val KEY = "key"
        const val VALUE = "value"

        /**
         * Create UriMatcher to match all requests
         */
        private var mUriMatcher: UriMatcher? = null
        fun extractStringFromCursor(cursor: Cursor?, defaultVal: String): String {
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(VALUE))
                    }
                }
                finally {
                    cursor.close()
                }
            }
            return defaultVal
        }

        fun extractIntFromCursor(cursor: Cursor?, defaultVal: Int): Int {
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(cursor.getColumnIndex(VALUE))
                    }
                }
                finally {
                    cursor.close()
                }
            }
            return defaultVal
        }

        fun extractLongFromCursor(cursor: Cursor?, defaultVal: Long): Long {
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(cursor.getColumnIndex(VALUE)).toLong()
                    }
                }
                finally {
                    cursor.close()
                }
            }
            return defaultVal
        }

        fun extractBooleanFromCursor(cursor: Cursor?, defaultVal: Boolean): Boolean {
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getInt(cursor.getColumnIndex(VALUE)) == 1
                    }
                }
                finally {
                    cursor.close()
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

        init {
            mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            // */* = wildcard  (name or file name / key)
            mUriMatcher!!.addURI(
                PROVIDER_NAME,
                "string/*/*",
                CODE_STRING
            )
            mUriMatcher!!.addURI(
                PROVIDER_NAME,
                "integer/*/*",
                CODE_INTEGER
            )
            mUriMatcher!!.addURI(
                PROVIDER_NAME,
                "long/*/*",
                CODE_LONG
            )
            mUriMatcher!!.addURI(
                PROVIDER_NAME,
                "boolean/*/*",
                CODE_BOOLEAN
            )
            mUriMatcher!!.addURI(
                PROVIDER_NAME,
                "prefs/*/",
                CODE_PREFS
            )
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
            val interactor =
                PreferenceInteractor(context!!, preferenceName)
            mPreferenceMap[preferenceName] = interactor
            interactor
        }
    }

    @Nullable
    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        val interactor: PreferenceInteractor? =
            getPreferenceInteractor(uri.pathSegments[1])
        when (mUriMatcher!!.match(uri)) {
            CODE_STRING -> {
                val s: String = uri.pathSegments[2]
                return if (interactor!!.hasKey(s)) preferenceToCursor(interactor.getString(s)) else null
            }
            CODE_INTEGER -> {
                val i: String = uri.pathSegments[2]
                return if (interactor!!.hasKey(i)) preferenceToCursor(interactor.getInt(i)) else null
            }
            CODE_LONG -> {
                val l: String = uri.pathSegments[2]
                return if (interactor!!.hasKey(l)) preferenceToCursor(interactor.getLong(l)) else null
            }
            CODE_BOOLEAN -> {
                val b: String = uri.pathSegments[2]
                return if (interactor!!.hasKey(b)) preferenceToCursor(if (interactor.getBoolean(b)) 1 else 0) else null
            }
        }
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        if (values != null) {
            val interactor: PreferenceInteractor? =
                getPreferenceInteractor(uri.pathSegments[1])
            val key = values.getAsString(KEY)
            when (mUriMatcher!!.match(uri)) {
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
        } else {
            throw IllegalArgumentException("Content Values are null!")
        }
        return 0
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        val interactor: PreferenceInteractor? =
            getPreferenceInteractor(uri.pathSegments[1])
        when (mUriMatcher!!.match(uri)) {
            CODE_REMOVE_KEY -> interactor!!.removePref(
                uri.pathSegments[2]
            )
            CODE_PREFS -> interactor!!.clearPreference()
            else -> throw IllegalStateException(" unsupported uri : $uri")
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
     * @return a Cursor object
    </T> */
    private fun <Any> preferenceToCursor(value: Any): MatrixCursor {
        val matrixCursor =
            MatrixCursor(arrayOf(VALUE), 1)
        val builder = matrixCursor.newRow()
        builder.add(value)
        return matrixCursor
    }
}