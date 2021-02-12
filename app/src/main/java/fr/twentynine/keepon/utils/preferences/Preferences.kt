package fr.twentynine.keepon.utils.preferences

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.glide.TimeoutIconStyle
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

    private val timeoutIconStyleJsonAdapter: JsonAdapter<TimeoutIconStyle> by lazy { Moshi.Builder().build().adapter(TimeoutIconStyle::class.java) }

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
        if (!getValueChange() || (Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis - getValueChangeTime()) >= 7000L) {
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

    fun setAppIsLaunched(value: Boolean) {
        multiPreferences
            .setBoolean(PREFS_FILENAME, APP_IS_LAUNCHED, value)
    }

    fun getTimeoutIconStyle(): TimeoutIconStyle {
        val timeoutIconStyleJson = multiPreferences.getString(PREFS_BACKUP_FILENAME, TIMEOUT_ICON_STYLE, "")
        if (timeoutIconStyleJson != "") {
            val timeoutIconStyle = timeoutIconStyleJsonAdapter.fromJson(timeoutIconStyleJson)
            if (timeoutIconStyle != null) {
                return timeoutIconStyle
            }
        }
        return convertOldTimeoutIconStyle()
    }

    fun setTimeoutIconStyle(value: TimeoutIconStyle) {
        val timeoutIconStyleJson = timeoutIconStyleJsonAdapter.toJson(value)
        if (timeoutIconStyleJson != null) {
            multiPreferences.setString(PREFS_BACKUP_FILENAME, TIMEOUT_ICON_STYLE, timeoutIconStyleJson)
        } else {
            multiPreferences.setString(PREFS_BACKUP_FILENAME, TIMEOUT_ICON_STYLE, "")
        }
    }

    private fun convertOldTimeoutIconStyle(): TimeoutIconStyle {
        val qsStyleFontSize = "qsStyleFontSize"
        val qsStyleFontSkew = "qsStyleFontSkew"
        val qsStyleFontSpacing = "qsStyleFontSpacing"
        val qsStyleTypefaceSansSerif = "qsStyleTypefaceSansSerif"
        val qsStyleTypefaceSerif = "qsStyleTypefaceSerif"
        val qsStyleTypefaceMonospace = "qsStyleTypefaceMonospace"
        val qsStyleTextFill = "qsStyleTextFill"
        val qsStyleTextFillStroke = "qsStyleTextFillStroke"
        val qsStyleTextStroke = "qsStyleTextStroke"
        val qsStyleFontBold = "qsStyleFontBold"
        val qsStyleFontUnderline = "qsStyleFontUnderline"
        val qsStyleFontSMCP = "qsStyleFontSMCP"

        // Define new timeout icon style
        val newTimeoutIconStyle = TimeoutIconStyle(
            multiPreferences.getInt(PREFS_BACKUP_FILENAME, qsStyleFontSize, 0),
            multiPreferences.getInt(PREFS_BACKUP_FILENAME, qsStyleFontSkew, 0),
            multiPreferences.getInt(PREFS_BACKUP_FILENAME, qsStyleFontSpacing, 0),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTypefaceSansSerif, true),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTypefaceSerif, false),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTypefaceMonospace, false),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleFontBold, true),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleFontUnderline, false),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleFontSMCP, false),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTextFill, true),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTextFillStroke, false),
            multiPreferences.getBoolean(PREFS_BACKUP_FILENAME, qsStyleTextStroke, false)
        )

        // Set new timeout style preference
        setTimeoutIconStyle(newTimeoutIconStyle)

        // Clear previous style preferences
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontSize)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontSkew)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontSpacing)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTypefaceSansSerif)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTypefaceSerif)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTypefaceMonospace)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontBold)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontUnderline)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleFontSMCP)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTextFill)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTextFillStroke)
        multiPreferences.removePreference(PREFS_BACKUP_FILENAME, qsStyleTextStroke)

        return newTimeoutIconStyle
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
        private const val TIMEOUT_ICON_STYLE = "timeoutIconStyle"
        private const val APP_REVIEW_ASKED = "appReviewAsked"
        private const val APP_LAUNCH_COUNT = "appLaunchCount"
        private const val APP_IS_LAUNCHED = "appIsLaunched"

        private const val DEFAULT_TIMEOUT = 60000
    }
}
