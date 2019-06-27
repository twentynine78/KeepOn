package fr.twentynine.keepon.utils.preferences

import android.content.Context
import android.content.SharedPreferences

// Preference Interactor class : Accesses Shared Preferences and returns a (Matrix) Cursor Object
internal class PreferenceInteractor(context: Context, preferenceName: String) {

    private val mSharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)


    fun getString(key: String): String? {
        return mSharedPreferences.getString(key, "")
    }

    fun setString(key: String, value: String) {
        mSharedPreferences.edit().putString(key, value).apply()
    }


    fun getInt(key: String): Int {
        return mSharedPreferences.getInt(key, -1)
    }

    fun setInt(key: String, value: Int) {
        mSharedPreferences.edit().putInt(key, value).apply()
    }


    fun getLong(key: String): Long {
        return mSharedPreferences.getLong(key, (-1).toLong())
    }

    fun setLong(key: String, value: Long) {
        mSharedPreferences.edit().putLong(key, value).apply()
    }


    fun getBoolean(key: String): Boolean {
        return mSharedPreferences.getBoolean(key, false)
    }

    fun setBoolean(key: String, value: Boolean) {
        mSharedPreferences.edit().putBoolean(key, value).apply()
    }


    fun removePref(key: String) {
        mSharedPreferences.edit().remove(key).apply()
    }

    fun clearPreference() {
        mSharedPreferences.edit().clear().apply()
    }
}
