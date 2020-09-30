package fr.twentynine.keepon.utils.preferences

import android.content.Context


object Preferences {
    private const val PREFS_FILENAME = "keepon_prefs"
    private const val PREFS_BACKUP_FILENAME = "keepon_prefs_backup"

    private const val ORIGINAL_TIMEOUT = "originalTimeout"
    private const val SELECTED_TIMEOUT = "selectedTimeout"
    private const val RESET_TIMEOUT_ON_SCREEN_OFF = "resetTimeoutOnScreenOff"
    private const val SKIP_INTRO = "skipIntro"
    private const val DARK_THEME = "darkTheme"
    private const val VALUE_CHANGE_INT = "valueChangeInt"
    private const val TILE_ADDED = "tileAdded"
    private const val NEW_VALUE = "newValue"
    private const val PREVIOUS_VALUE = "previousValue"
    private const val QS_STYLE_FONT_SIZE = "qsStyleFontSize"
    private const val QS_STYLE_FONT_SKEW = "qsStyleFontSkew"
    private const val QS_STYLE_FONT_SPACING = "qsStyleFontSpacing"
    private const val QS_STYLE_TYPEFACE_SANS_SERIF = "qsStyleTypefaceSansSerif"
    private const val QS_STYLE_TYPEFACE_SERIF = "qsStyleTypefaceSerif"
    private const val QS_STYLE_TYPEFACE_MONOSPACE = "qsStyleTypefaceMonospace"
    private const val QS_STYLE_TEXT_FILL = "qsStyleTextFill"
    private const val QS_STYLE_TEXT_FILL_STROKE = "qsStyleTextFillStroke"
    private const val QS_STYLE_TEXT_STROKE = "qsStyleTextStroke"
    private const val QS_STYLE_FONT_BOLD = "qsStyleFontBold"
    private const val QS_STYLE_FONT_UNDERLINE = "qsStyleFontUnderline"
    private const val QS_STYLE_FONT_SMCP = "qsStyleFontSMCP"
    private const val GENERATE_BOOL_ASKED = "generateBoolAsked"
    private const val GENERATE_LAUNCH_COUNT = "generateLaunchCount"

    fun getOriginalTimeout(context: Context): Int {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getInt(ORIGINAL_TIMEOUT, 0)
    }

    fun setOriginalTimeout(value: Int, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setInt(ORIGINAL_TIMEOUT, value)
    }


    fun getSelectedTimeout(context: Context): ArrayList<Int> {
        return getListIntFromString(
            MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
                .getString(SELECTED_TIMEOUT, "")
        )
    }

    fun setSelectedTimeout(value: ArrayList<Int>, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setString(SELECTED_TIMEOUT, getStringFromListInt(value))
    }


    fun getResetTimeoutOnScreenOff(context: Context): Boolean {
        // Reverse boolean to prevent mistake on default value on true
        return !MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, false)
    }

    fun setResetTimeoutOnScreenOff(value: Boolean, context: Context) {
        // Reverse boolean to prevent mistake on default value on true
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(RESET_TIMEOUT_ON_SCREEN_OFF, !value)
    }


    fun getSkipIntro(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(SKIP_INTRO, false)
    }

    fun setSkipIntro(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(SKIP_INTRO, value)
    }


    fun getDarkTheme(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getBoolean(DARK_THEME, false)
    }

    fun setDarkTheme(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setBoolean(DARK_THEME, value)
    }


    fun getValueChange(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getInt(VALUE_CHANGE_INT, 0) != 0
    }

    fun setValueChange(value: Boolean, context: Context) {
        val result =
            if (value) {
                MultiPreferences(PREFS_FILENAME, context.contentResolver).getInt(VALUE_CHANGE_INT, 0) + 1
            } else {
                if (MultiPreferences(PREFS_FILENAME, context.contentResolver).getInt(
                        VALUE_CHANGE_INT, 0) > 0) {
                    MultiPreferences(PREFS_FILENAME, context.contentResolver).getInt(
                        VALUE_CHANGE_INT, 0) - 1
                } else {
                    0
                }
            }

        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setInt(VALUE_CHANGE_INT, result)
    }


    fun getTileAdded(context: Context): Boolean {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getBoolean(TILE_ADDED, false)
    }

    fun setTileAdded(value: Boolean, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setBoolean(TILE_ADDED, value)
    }


    fun getNewValue(context: Context): Int {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getInt(NEW_VALUE, 0)
    }

    fun setNewValue(value: Int, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setInt(NEW_VALUE, value)
    }


    fun getPreviousValue(context: Context): Int {
        return MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .getInt(PREVIOUS_VALUE, 0)
    }

    fun setPreviousValue(value: Int, context: Context) {
        MultiPreferences(PREFS_FILENAME, context.contentResolver)
            .setInt(PREVIOUS_VALUE, value)
    }


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

    fun getQSStyleFontSize(context: Context): Int {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getInt(QS_STYLE_FONT_SIZE, 0)
    }

    fun setQSStyleFontSize(value: Int, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setInt(QS_STYLE_FONT_SIZE, value)
    }

    fun getQSStyleFontSkew(context: Context): Int {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getInt(QS_STYLE_FONT_SKEW, 0)
    }

    fun setQSStyleFontSkew(value: Int, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setInt(QS_STYLE_FONT_SKEW, value)
    }

    fun getQSStyleFontSpacing(context: Context): Int {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getInt(QS_STYLE_FONT_SPACING, 0)
    }

    fun setQSStyleFontSpacing(value: Int, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setInt(QS_STYLE_FONT_SPACING, value)
    }

    fun getQSStyleFontBold(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_FONT_BOLD, true)
    }

    fun setQSStyleFontBold(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_FONT_BOLD, value)
    }

    fun getQSStyleFontUnderline(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_FONT_UNDERLINE, false)
    }

    fun setQSStyleFontUnderline(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_FONT_UNDERLINE, value)
    }

    fun getQSStyleFontSMCP(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_FONT_SMCP, false)
    }

    fun setQSStyleFontSMCP(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_FONT_SMCP, value)
    }

    fun getQSStyleTypefaceSansSerif(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TYPEFACE_SANS_SERIF, true)
    }

    fun setQSStyleTypefaceSansSerif(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TYPEFACE_SANS_SERIF, value)
    }

    fun getQSStyleTypefaceSerif(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TYPEFACE_SERIF, false)
    }

    fun setQSStyleTypefaceSerif(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TYPEFACE_SERIF, value)
    }

    fun getQSStyleTypefaceMonospace(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TYPEFACE_MONOSPACE, false)
    }

    fun setQSStyleTypefaceMonospace(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TYPEFACE_MONOSPACE, value)
    }

    fun getQSStyleTextFill(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TEXT_FILL, true)
    }

    fun setQSStyleTextFill(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TEXT_FILL, value)
    }

    fun getQSStyleTextFillStroke(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TEXT_FILL_STROKE, false)
    }

    fun setQSStyleTextFillStroke(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TEXT_FILL_STROKE, value)
    }

    fun getQSStyleTextStroke(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(QS_STYLE_TEXT_STROKE, false)
    }

    fun setQSStyleTextStroke(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(QS_STYLE_TEXT_STROKE, value)
    }

    fun getGenerateBoolAsked(context: Context): Boolean {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getBoolean(GENERATE_BOOL_ASKED, false)
    }

    fun setGenerateBoolAsked(value: Boolean, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setBoolean(GENERATE_BOOL_ASKED, value)
    }

    fun getGenerateLaunchCount(context: Context): Long {
        return MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .getLong(GENERATE_LAUNCH_COUNT, 0)
    }

    fun setGenerateLaunchCount(value: Long, context: Context) {
        MultiPreferences(PREFS_BACKUP_FILENAME, context.contentResolver)
            .setLong(GENERATE_LAUNCH_COUNT, value)
    }
}