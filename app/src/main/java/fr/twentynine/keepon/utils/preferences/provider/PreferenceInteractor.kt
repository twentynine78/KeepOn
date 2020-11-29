package fr.twentynine.keepon.utils.preferences.provider

import android.content.Context
import android.content.SharedPreferences

/**
 * Preference Interactor class
 *
 *
 * - Accesses Shared Preferences and returns a (Matrix) Cursor Object
 */
class PreferenceInteractor(context: Context, preferenceName: String?) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

    fun hasKey(key: String?): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getString(key: String?): String? {
        return sharedPreferences.getString(key, DEFAULT_STRING)
    }

    fun setString(key: String?, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getInt(key: String?): Int {
        return sharedPreferences.getInt(key, DEFAULT_INT)
    }

    fun setInt(key: String?, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences.getLong(key, DEFAULT_LONG)
    }

    fun setLong(key: String?, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String?): Boolean {
        return sharedPreferences.getBoolean(key, DEFAULT_BOOLEAN)
    }

    fun setBoolean(key: String?, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun removePref(key: String?) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clearPreference() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val DEFAULT_STRING = ""
        private const val DEFAULT_INT = -1
        private const val DEFAULT_LONG = -1L
        private const val DEFAULT_BOOLEAN = false
    }
}
