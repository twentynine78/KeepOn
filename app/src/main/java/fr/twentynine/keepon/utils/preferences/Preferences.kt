package fr.twentynine.keepon.utils.preferences

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.preferences.provider.MultiPreferences
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Suppress("TooManyFunctions")
@ApplicationScope
@Singleton
@InjectConstructor
class Preferences(application: Application, private val contentResolver: ContentResolver) {

    private val multiPreferences: MultiPreferences by lazy()

    private val mDPM = application.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val timeoutValueArray = arrayListOf(
        15000,
        30000,
        60000,
        120000,
        300000,
        600000,
        1800000,
        3600000,
        Int.MAX_VALUE
    )

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    fun getKeepOnState(): Boolean {
        return getCurrentTimeout() != getOriginalTimeout()
    }

    fun getTimeoutValueArray(): ArrayList<Int> {
        return timeoutValueArray
    }

    fun getNextTimeoutValue(): Int {
        val allTimeouts = getTimeoutValueArray()

        val availableTimeout: ArrayList<Int> = ArrayList()
        availableTimeout.addAll(getSelectedTimeout())
        availableTimeout.remove(getOriginalTimeout())
        availableTimeout.add(getOriginalTimeout())
        availableTimeout.sort()

        val currentTimeout = getCurrentTimeout()
        var allCurrentIndex = allTimeouts.indexOf(currentTimeout)

        // Check for DevicePolicy restriction
        var adminTimeout = mDPM.getMaximumTimeToLock(null)
        if (adminTimeout == 0L) adminTimeout = Long.MAX_VALUE

        for (i in 0 until allTimeouts.size) {
            if (allCurrentIndex == allTimeouts.size - 1 || allCurrentIndex == -1) {
                allCurrentIndex = 0
            } else {
                allCurrentIndex++
            }
            if (availableTimeout.indexOf(allTimeouts[allCurrentIndex]) != -1 && allTimeouts[allCurrentIndex] <= adminTimeout) {
                return allTimeouts[allCurrentIndex]
            }
        }
        return getCurrentTimeout()
    }

    fun getOriginalTimeout(): Int {
        val origTimeout = multiPreferences
            .getInt(PREFS_FILENAME, ORIGINAL_TIMEOUT, 0)
        return if (origTimeout == 0) {
            getCurrentTimeout()
        } else {
            origTimeout
        }
    }

    fun setOriginalTimeout(value: Int) {
        multiPreferences
            .setInt(PREFS_FILENAME, ORIGINAL_TIMEOUT, value)
    }

    fun getCurrentTimeout(): Int {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT, DEFAULT_TIMEOUT
        )
    }

    fun setTimeout(timeout: Int) {
        if (!getValueChange() || (Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis - getValueChangeTime()) >= 3000L) {
            try {
                setValueChange(true)
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT, timeout
                )
            } catch (e: Exception) {
                setValueChange(false)
                e.printStackTrace()
            }
        }
    }

    fun getSelectedTimeout(): ArrayList<Int> {
        return getListIntFromString(
            multiPreferences
                .getString(PREFS_BACKUP_FILENAME, SELECTED_TIMEOUT, "")
        )
    }

    fun setSelectedTimeout(value: ArrayList<Int>) {
        multiPreferences
            .setString(PREFS_BACKUP_FILENAME, SELECTED_TIMEOUT, getStringFromListInt(value))
    }

    fun getResetTimeoutOnScreenOff(): Boolean {
        // Reverse boolean to prevent mistake on default value on true
        return !multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, RESET_TIMEOUT_ON_SCREEN_OFF, false)
    }

    fun setResetTimeoutOnScreenOff(value: Boolean) {
        // Reverse boolean to prevent mistake on default value on true
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, RESET_TIMEOUT_ON_SCREEN_OFF, !value)
    }

    fun getSkipIntro(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, SKIP_INTRO, false)
    }

    fun setSkipIntro(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, SKIP_INTRO, value)
    }

    fun getValueChange(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_FILENAME, VALUE_CHANGE_BOOL, false)
    }

    fun setValueChange(value: Boolean) {
        multiPreferences
            .setLong(PREFS_FILENAME, VALUE_CHANGE_TIME, Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis)
        multiPreferences
            .setBoolean(PREFS_FILENAME, VALUE_CHANGE_BOOL, value)
    }

    fun getValueChangeTime(): Long {
        return multiPreferences
            .getLong(PREFS_FILENAME, VALUE_CHANGE_TIME, 0L)
    }

    fun getTileAdded(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_FILENAME, TILE_ADDED, false)
    }

    fun setTileAdded(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_FILENAME, TILE_ADDED, value)
    }

    fun getNewValue(): Int {
        return multiPreferences
            .getInt(PREFS_FILENAME, NEW_VALUE, 0)
    }

    fun setNewValue(value: Int) {
        multiPreferences
            .setInt(PREFS_FILENAME, NEW_VALUE, value)
    }

    fun getPreviousValue(): Int {
        return multiPreferences
            .getInt(PREFS_FILENAME, PREVIOUS_VALUE, 0)
    }

    fun setPreviousValue(value: Int) {
        multiPreferences
            .setInt(PREFS_FILENAME, PREVIOUS_VALUE, value)
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
                if (string.isNotEmpty()) {
                    resultList.add(string.toInt())
                }
            }
        }
        return resultList
    }

    fun getDarkTheme(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_FILENAME, DARK_THEME, false)
    }

    fun setDarkTheme(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_FILENAME, DARK_THEME, value)
    }

    fun getQSStyleFontSize(): Int {
        return multiPreferences
            .getInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SIZE, 0)
    }

    fun setQSStyleFontSize(value: Int) {
        multiPreferences
            .setInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SIZE, value)
    }

    fun getQSStyleFontSkew(): Int {
        return multiPreferences
            .getInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SKEW, 0)
    }

    fun setQSStyleFontSkew(value: Int) {
        multiPreferences
            .setInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SKEW, value)
    }

    fun getQSStyleFontSpacing(): Int {
        return multiPreferences
            .getInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SPACING, 0)
    }

    fun setQSStyleFontSpacing(value: Int) {
        multiPreferences
            .setInt(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SPACING, value)
    }

    fun getQSStyleFontBold(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_BOLD, true)
    }

    fun setQSStyleFontBold(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_BOLD, value)
    }

    fun getQSStyleFontUnderline(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_UNDERLINE, false)
    }

    fun setQSStyleFontUnderline(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_UNDERLINE, value)
    }

    fun getQSStyleFontSMCP(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SMCP, false)
    }

    fun setQSStyleFontSMCP(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_FONT_SMCP, value)
    }

    fun getQSStyleTypefaceSansSerif(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_SANS_SERIF, true)
    }

    fun setQSStyleTypefaceSansSerif(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_SANS_SERIF, value)
    }

    fun getQSStyleTypefaceSerif(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_SERIF, false)
    }

    fun setQSStyleTypefaceSerif(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_SERIF, value)
    }

    fun getQSStyleTypefaceMonospace(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_MONOSPACE, false)
    }

    fun setQSStyleTypefaceMonospace(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TYPEFACE_MONOSPACE, value)
    }

    fun getQSStyleTextFill(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_FILL, true)
    }

    fun setQSStyleTextFill(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_FILL, value)
    }

    fun getQSStyleTextFillStroke(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_FILL_STROKE, false)
    }

    fun setQSStyleTextFillStroke(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_FILL_STROKE, value)
    }

    fun getQSStyleTextStroke(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_STROKE, false)
    }

    fun setQSStyleTextStroke(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, QS_STYLE_TEXT_STROKE, value)
    }

    fun getAppReviewAsked(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_BACKUP_FILENAME, APP_REVIEW_ASKED, false)
    }

    fun setAppReviewAsked(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_BACKUP_FILENAME, APP_REVIEW_ASKED, value)
    }

    fun getAppLaunchCount(): Long {
        return multiPreferences
            .getLong(PREFS_BACKUP_FILENAME, APP_LAUNCH_COUNT, 0)
    }

    fun setAppLaunchCount(value: Long) {
        multiPreferences
            .setLong(PREFS_BACKUP_FILENAME, APP_LAUNCH_COUNT, value)
    }

    fun getAppIsLaunched(): Boolean {
        return multiPreferences
            .getBoolean(PREFS_FILENAME, APP_IS_LAUNCHED, false)
    }

    fun setAppILaunched(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_FILENAME, APP_IS_LAUNCHED, value)
    }

    companion object {
        private const val PREFS_FILENAME = "keepon_prefs"
        private const val PREFS_BACKUP_FILENAME = "keepon_prefs_backup"

        private const val ORIGINAL_TIMEOUT = "originalTimeout"
        private const val SELECTED_TIMEOUT = "selectedTimeout"
        private const val RESET_TIMEOUT_ON_SCREEN_OFF = "resetTimeoutOnScreenOff"
        private const val SKIP_INTRO = "skipIntro"
        private const val VALUE_CHANGE_BOOL = "valueChangeBool"
        private const val VALUE_CHANGE_TIME = "valueChangeTime"
        private const val TILE_ADDED = "tileAdded"
        private const val NEW_VALUE = "newValue"
        private const val PREVIOUS_VALUE = "previousValue"
        private const val DARK_THEME = "darkTheme"
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
        private const val APP_REVIEW_ASKED = "appReviewAsked"
        private const val APP_LAUNCH_COUNT = "appLaunchCount"
        private const val APP_IS_LAUNCHED = "appIsLaunched"

        private const val DEFAULT_TIMEOUT = 60000
    }
}
