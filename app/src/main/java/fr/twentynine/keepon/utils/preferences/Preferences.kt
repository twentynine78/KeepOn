package fr.twentynine.keepon.utils.preferences

import android.content.Context


object Preferences {

    private const val PREFS_FILENAME = "keepon_prefs"
    private const val PREFS_BACKUP_FILENAME = "keepon_prefs_backup"

    private const val ORIGINAL_TIMEOUT = "originalTimeout"
    private const val SELECTED_TIMEOUT = "selectedTimeout"
    private const val KEEP_ON = "keepOn"
    private const val RESET_TIMEOUT_ON_SCREEN_OFF = "resetTimeoutOnScreenOff"
    private const val SKIP_INTRO = "skipIntro"
    private const val DARK_THEME = "darkTheme"
    private const val VALUE_CHANGE = "valueChange"
    private const val TILE_ADDED = "tileAdded"


    private fun getStringFromListInt(listInt: ArrayList<Int>): String {
        var resultString = ""
        for (int: Int in listInt) {
            resultString += "$int|"
        }
        return resultString
    }

    private fun getListIntFromString(stringList: String?): ArrayList<Int> {
        val resultList: ArrayList<Int> = ArrayList()
        val tempList = stringList?.split("|")
        if (tempList != null) {
            for (string: String in tempList) {
                if (string.isNotEmpty())
                    resultList.add(string.toInt())
            }
        }
        return resultList
    }

    @JvmStatic fun getOriginalTimeout(context: Context): Int {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver).getInt(ORIGINAL_TIMEOUT, 0)
    }

    @JvmStatic fun setOriginalTimeout(value: Int, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver).setInt(ORIGINAL_TIMEOUT, value)
    }


    @JvmStatic fun getSelectedTimeout(context: Context): ArrayList<Int> {
        return getListIntFromString(MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).getString(SELECTED_TIMEOUT, ""))
    }

    @JvmStatic fun setSelectedTimeout(value: ArrayList<Int>, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).setString(SELECTED_TIMEOUT, getStringFromListInt(value))
    }


    @JvmStatic fun getKeepOn(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver).getBoolean(KEEP_ON, false)
    }

    @JvmStatic fun setKeepOn(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver).setBoolean(KEEP_ON, value)
    }


    @JvmStatic fun getResetTimeoutOnScreenOff(context: Context): Boolean {
        // Reverse boolean to prevent mistake on default value on true
        return !MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).getBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, false)
    }

    @JvmStatic fun setResetTimeoutOnScreenOff(value: Boolean, context: Context) {
        // Reverse boolean to prevent mistake on default value on true
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).setBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, !value)
    }


    @JvmStatic fun getSkipIntro(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).getBoolean(SKIP_INTRO, false)
    }

    @JvmStatic fun setSkipIntro(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver).setBoolean(SKIP_INTRO, value)
    }


    @JvmStatic fun getDarkTheme(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver).getBoolean(DARK_THEME, false)
    }

    @JvmStatic fun setDarkTheme(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver).setBoolean(DARK_THEME, value)
    }


    @JvmStatic fun getValueChange(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver).getBoolean(VALUE_CHANGE, false)
    }

    @JvmStatic fun setValueChange(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver).setBoolean(VALUE_CHANGE, value)
    }


    @JvmStatic fun getTileAdded(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver).getBoolean(TILE_ADDED, false)
    }

    @JvmStatic fun setTileAdded(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver).setBoolean(TILE_ADDED, value)
    }
    }
}