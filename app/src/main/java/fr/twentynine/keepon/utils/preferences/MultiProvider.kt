package fr.twentynine.keepon.utils.preferences

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import java.util.*

// Multi Preference provider class
class MultiProvider : ContentProvider() {

    // Map to hold all current Inter actors with shared preferences
    private val mPreferenceMap = HashMap<String, PreferenceInteractor>()


    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        /* Create a new Preference Interactor class based on the Preference File Name, of return the existing one
          from the map  - Preference (File) name comes form the Uri segment 2 */
        val interactor = getPreferenceInteractor(uri.pathSegments[1])

        when (mUriMatcher.match(uri)) {
            CODE_STRING -> return preferenceToCursor(interactor!!.getString(uri.pathSegments[2]))
            CODE_INTEGER -> return preferenceToCursor(interactor!!.getInt(uri.pathSegments[2]))
            CODE_LONG -> return preferenceToCursor(interactor!!.getLong(uri.pathSegments[2]))
            CODE_BOOLEAN -> return preferenceToCursor(if (interactor!!.getBoolean(uri.pathSegments[2])) 1 else 0)
        }
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        if (values != null) {
            /* Create a new Preference Interactor class based on the Preference File Name, of return the existing one
              from the map  - Preference (File) name comes form the Uri segment 2 */
            val interactor = getPreferenceInteractor(uri.pathSegments[1])

            val key = values.getAsString(KEY)

            when (mUriMatcher.match(uri)) {
                CODE_STRING -> {
                    val sValue = values.getAsString(VALUE)
                    interactor!!.setString(key, sValue)
                }
                CODE_INTEGER -> {
                    val iValue = values.getAsInteger(VALUE)!!
                    interactor!!.setInt(key, iValue)
                }
                CODE_LONG -> {
                    val lValue = values.getAsLong(VALUE)!!
                    interactor!!.setLong(key, lValue)
                }
                CODE_BOOLEAN -> {
                    val bValue = values.getAsBoolean(VALUE)!!
                    interactor!!.setBoolean(key, bValue)
                }
            }
        } else {
            throw IllegalArgumentException(" Content Values are null!")
        }
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        /* Create a new Preference Interactor class based on the Preference File Name, of return the existing one
          from the map  - Preference (File) name comes form the Uri segment 1 */
        val interactor = getPreferenceInteractor(uri.pathSegments[1])

        when (mUriMatcher.match(uri)) {
            CODE_STRING, CODE_INTEGER, CODE_LONG, CODE_BOOLEAN -> interactor!!.removePref(uri.pathSegments[2])
            CODE_PREFS -> interactor!!.clearPreference()
            else -> throw IllegalStateException(" unsupported uri : $uri")
        }
        return 0
    }

    // NOT SUPPORTED
    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    /* Get a new Preference Interactor, or return a previously used Interactor
      @param preferenceName the name of the preference file
      @return a new interactor, or current one in the map */
    private fun getPreferenceInteractor(preferenceName: String): PreferenceInteractor? {
        return if (mPreferenceMap.containsKey(preferenceName)) {
            mPreferenceMap[preferenceName]
        } else {
            val interactor = PreferenceInteractor(context!!, preferenceName)
            mPreferenceMap[preferenceName] = interactor
            interactor
        }
    }

    /* Convert a value into a cursor object using a Matrix Cursor
      @param value the value to be converted
      @param <T> generic object type
      @return a Cursor object
    </T> */
    private fun <T> preferenceToCursor(value: T): MatrixCursor {
        val matrixCursor = MatrixCursor(arrayOf(VALUE), 1)
        val builder = matrixCursor.newRow()
        builder.add(value)

        return matrixCursor
    }

    companion object {
        // Create UriMatcher to match all requests
        private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        // Define provider name
        private const val PROVIDER_NAME = "fr.twentynine.keepon.utils.preferences.MultiProvider"

        // Define all Content Urls for each type, String, int, long & boolean
        private const val URL_STRING = "content://$PROVIDER_NAME/string/"
        private const val URL_INT = "content://$PROVIDER_NAME/integer/"
        private const val URL_LONG = "content://$PROVIDER_NAME/long/"
        private const val URL_BOOLEAN = "content://$PROVIDER_NAME/boolean/"
        // Special URL just for clearing preferences
        private const val URL_PREFERENCES = "content://$PROVIDER_NAME/prefs/"

        const val CODE_STRING = 1
        const val CODE_INTEGER = 2
        const val CODE_LONG = 3
        const val CODE_BOOLEAN = 4
        const val CODE_PREFS = 5

        const val KEY = "key"
        const val VALUE = "value"

        init {
            // */* = wildcard  (name or file name / key)
            mUriMatcher.addURI(PROVIDER_NAME, "string/*/*", CODE_STRING)
            mUriMatcher.addURI(PROVIDER_NAME, "integer/*/*", CODE_INTEGER)
            mUriMatcher.addURI(PROVIDER_NAME, "long/*/*", CODE_LONG)
            mUriMatcher.addURI(PROVIDER_NAME, "boolean/*/*", CODE_BOOLEAN)
            mUriMatcher.addURI(PROVIDER_NAME, "prefs/*/", CODE_PREFS)
        }

        fun createQueryUri(prefFileName: String, key: String, prefType: Int): Uri {
            return when (prefType) {
                CODE_STRING -> Uri.parse("$URL_STRING$prefFileName/$key")
                CODE_INTEGER -> Uri.parse("$URL_INT$prefFileName/$key")
                CODE_LONG -> Uri.parse("$URL_LONG$prefFileName/$key")
                CODE_BOOLEAN -> Uri.parse("$URL_BOOLEAN$prefFileName/$key")
                CODE_PREFS -> Uri.parse("$URL_PREFERENCES$prefFileName/$key")
                else -> throw IllegalStateException("Not Supported Type : $prefType")
            }
        }
    }


}
