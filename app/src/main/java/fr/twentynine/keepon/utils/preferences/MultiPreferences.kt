package fr.twentynine.keepon.utils.preferences

import android.content.ContentValues
import android.content.Context

// Multi Preference class : allows access to Shared Preferences across processes through a Content Provider
object MultiPreferences {
    
    private val multiProvider = MultiProvider


    @JvmStatic fun setString(key: String, value: String, mFileName: String, mContext: Context) {
        val updateUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_STRING)

        val contentValues = ContentValues()
        contentValues.put(multiProvider.KEY, key)
        contentValues.put(multiProvider.VALUE, value)

        mContext.contentResolver.update(updateUri, contentValues, null, null)
    }

    @JvmStatic fun getString(key: String, defaultValue: String, mFileName: String, mContext: Context): String {
        var value = defaultValue

        val queryUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_STRING)

        val cursor = mContext.contentResolver.query(queryUri, null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val tempValue = cursor.getString(cursor.getColumnIndexOrThrow(multiProvider.VALUE))
            if (tempValue != "") {
                value = tempValue
            }
            cursor.close()
        }
        return value
    }


    @JvmStatic fun setInt(key: String, value: Int, mFileName: String, mContext: Context) {
        val updateUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_INTEGER)

        val contentValues = ContentValues()
        contentValues.put(multiProvider.KEY, key)
        contentValues.put(multiProvider.VALUE, value)

        mContext.contentResolver.update(updateUri, contentValues, null, null)
    }

    @JvmStatic fun getInt(key: String, defaultValue: Int, mFileName: String, mContext: Context): Int {
        var value = defaultValue

        val queryUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_INTEGER)

        val cursor = mContext.contentResolver.query(queryUri, null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val tempValue = cursor.getInt(cursor.getColumnIndexOrThrow(multiProvider.VALUE))
            if (tempValue != -1) {
                value = tempValue
            }
            cursor.close()
        }
        return value
    }


    @JvmStatic fun setLong(key: String, value: Long, mFileName: String, mContext: Context) {
        val updateUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_LONG)

        val contentValues = ContentValues()
        contentValues.put(multiProvider.KEY, key)
        contentValues.put(multiProvider.VALUE, value)

        mContext.contentResolver.update(updateUri, contentValues, null, null)
    }

    @JvmStatic fun getLong(key: String, defaultValue: Long, mFileName: String, mContext: Context): Long {
        var value = defaultValue

        val queryUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_LONG)

        val cursor = mContext.contentResolver.query(queryUri, null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val tempValue = cursor.getInt(cursor.getColumnIndexOrThrow(multiProvider.VALUE))
            if (tempValue != -1) {
                value = tempValue.toLong()
            }
            cursor.close()
        }
        return value
    }


    @JvmStatic fun setBoolean(key: String, value: Boolean, mFileName: String, mContext: Context) {
        val updateUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_BOOLEAN)

        val contentValues = ContentValues()
        contentValues.put(multiProvider.KEY, key)
        contentValues.put(multiProvider.VALUE, value)

        mContext.contentResolver.update(updateUri, contentValues, null, null)
    }

    @JvmStatic fun getBoolean(key: String, defaultValue: Boolean, mFileName: String, mContext: Context): Boolean {
        var value = if (defaultValue) 1 else 0

        val queryUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_BOOLEAN)

        val cursor = mContext.contentResolver.query(queryUri, null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val tempValue = cursor.getInt(cursor.getColumnIndexOrThrow(multiProvider.VALUE))
            if (tempValue != value) {
                value = tempValue
            }
            cursor.close()
        }
        return value == 1
    }


    @JvmStatic fun removePreference(key: String, mFileName: String, mContext: Context) {
        val deleteUri = multiProvider.createQueryUri(mFileName, key, multiProvider.CODE_INTEGER)
        mContext.contentResolver.delete(deleteUri, null, null)
    }

    @JvmStatic fun clearPreferences(mFileName: String, mContext: Context) {
        val clearPrefsUri = multiProvider.createQueryUri(mFileName, "", multiProvider.CODE_PREFS)
        mContext.contentResolver.delete(clearPrefsUri, null, null)
    }
}
