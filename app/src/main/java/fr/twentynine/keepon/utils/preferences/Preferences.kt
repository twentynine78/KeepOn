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
        return MultiPreferences.getInt(ORIGINAL_TIMEOUT, 0, PREFS_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setOriginalTimeout(value: Int, context: Context) {
        MultiPreferences.setInt(ORIGINAL_TIMEOUT, value, PREFS_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getSelectedTimeout(context: Context): ArrayList<Int> {
        return getListIntFromString(MultiPreferences.getString(SELECTED_TIMEOUT, "", PREFS_BACKUP_FILENAME, context.applicationContext))
    }

    @JvmStatic fun setSelectedTimeout(value: ArrayList<Int>, context: Context) {
        MultiPreferences.setString(SELECTED_TIMEOUT, getStringFromListInt(value), PREFS_BACKUP_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getKeepOn(context: Context): Boolean {
        return MultiPreferences.getBoolean(KEEP_ON, false, PREFS_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setKeepOn(value: Boolean, context: Context) {
        MultiPreferences.setBoolean(KEEP_ON, value, PREFS_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getResetTimeoutOnScreenOff(context: Context): Boolean {
        // Reverse boolean to prevent mistake on default value on true
        return !MultiPreferences.getBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, false, PREFS_BACKUP_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setResetTimeoutOnScreenOff(value: Boolean, context: Context) {
        // Reverse boolean to prevent mistake on default value on true
        MultiPreferences.setBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, !value, PREFS_BACKUP_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getSkipIntro(context: Context): Boolean {
        return MultiPreferences.getBoolean(SKIP_INTRO, false, PREFS_BACKUP_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setSkipIntro(value: Boolean, context: Context) {
        MultiPreferences.setBoolean(SKIP_INTRO, value, PREFS_BACKUP_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getDarkTheme(context: Context): Boolean {
        return MultiPreferences.getBoolean(DARK_THEME, false, PREFS_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setDarkTheme(value: Boolean, context: Context) {
        MultiPreferences.setBoolean(DARK_THEME, value, PREFS_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getValueChange(context: Context): Boolean {
        return MultiPreferences.getBoolean(VALUE_CHANGE, false, PREFS_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setValueChange(value: Boolean, context: Context) {
        MultiPreferences.setBoolean(VALUE_CHANGE, value, PREFS_FILENAME, context.applicationContext)
    }


    @JvmStatic fun getTileAdded(context: Context): Boolean {
        return MultiPreferences.getBoolean(TILE_ADDED, false, PREFS_FILENAME, context.applicationContext)
    }

    @JvmStatic fun setTileAdded(value: Boolean, context: Context) {
        MultiPreferences.setBoolean(TILE_ADDED, value, PREFS_FILENAME, context.applicationContext)
    }
}