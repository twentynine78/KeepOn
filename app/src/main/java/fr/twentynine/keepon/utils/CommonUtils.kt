package fr.twentynine.keepon.utils

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.TileService
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.receivers.ApplicationReceiver
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.services.ScreenTimeoutObserverService
import fr.twentynine.keepon.ui.SplashScreen
import fr.twentynine.keepon.utils.glide.TimeoutIconData
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import javax.inject.Singleton

@ApplicationScope
@Singleton
@InjectConstructor
class CommonUtils(private val application: Application) {

    private val preferences: Preferences by lazy()
    private val glideApp: RequestManager by lazy()

    private val updateIntent: Intent by lazy { Intent(ACTION_MAIN_ACTIVITY_UPDATE_UI).setPackage(application.packageName).also {
        it.action = ACTION_MAIN_ACTIVITY_UPDATE_UI
    } }
    private val missingIntent: Intent by lazy { Intent(ACTION_MAIN_ACTIVITY_MISSING_SETTINGS).setPackage(application.packageName).also {
        it.action = ACTION_MAIN_ACTIVITY_MISSING_SETTINGS
    } }
    private val startScreenOffReceiverServiceIntent: Intent by lazy { Intent(application.applicationContext, ScreenOffReceiverService::class.java) }
    private val stopScreenOffReceiverServiceIntent: Intent by lazy { Intent(application.applicationContext, ScreenOffReceiverService::class.java).also {
        it.action = ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
    } }
    private val startScreenTimeoutObserverServiceIntent: Intent by lazy { Intent(application.applicationContext, ScreenTimeoutObserverService::class.java) }
    private val manageShortcutsIntent: Intent by lazy { Intent(application, ApplicationReceiver::class.java).also {
        it.action = ACTION_MANAGE_SHORTCUTS
    } }
    private val shortcutIntent: Intent by lazy { Intent(application, SplashScreen::class.java).also {
        it.action = ACTION_SHORTCUT_SET_TIMEOUT
    } }
    private val shortcutManager: ShortcutManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            application.getSystemService(ShortcutManager::class.java)
        } else {
            null
        }
    }
    private val timeoutMap: ArrayMap<Int, Int> by lazy {
        arrayMapOf(
            -43 to R.string.timeout_previous,
            -42 to R.string.timeout_restore,
            preferences.getTimeoutValueArray()[0] to R.string.timeout_15_seconds,
            preferences.getTimeoutValueArray()[1] to R.string.timeout_30_seconds,
            preferences.getTimeoutValueArray()[2] to R.string.timeout_1_minute,
            preferences.getTimeoutValueArray()[3] to R.string.timeout_2_minutes,
            preferences.getTimeoutValueArray()[4] to R.string.timeout_5_minutes,
            preferences.getTimeoutValueArray()[5] to R.string.timeout_10_minutes,
            preferences.getTimeoutValueArray()[6] to R.string.timeout_30_minutes,
            preferences.getTimeoutValueArray()[7] to R.string.timeout_1_hour,
            preferences.getTimeoutValueArray()[8] to R.string.timeout_infinite
        )
    }

    private lateinit var manageShortcutJob: Job
    private lateinit var stringTimeout: String

    private var newTimeout: Int = 0

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    fun startScreenOffReceiverService() {
        if (!ScreenOffReceiverService.isRunning) {
            ContextCompat.startForegroundService(application.applicationContext, startScreenOffReceiverServiceIntent)
        }
    }

    fun stopScreenOffReceiverService() {
        if (ScreenOffReceiverService.isRunning) {
            ContextCompat.startForegroundService(application.applicationContext, stopScreenOffReceiverServiceIntent)
        }
    }

    fun startScreenTimeoutObserverService() {
        if (!ScreenTimeoutObserverService.isRunning) {
            ContextCompat.startForegroundService(application.applicationContext, startScreenTimeoutObserverServiceIntent)
        }
    }

    fun sendBroadcastUpdateMainUI() {
        application.sendBroadcast(updateIntent)
    }

    fun sendBroadcastMissingSettings() {
        application.sendBroadcast(missingIntent)
    }

    fun updateQSTile() {
        TileService.requestListeningState(application.applicationContext, ComponentName(application, KeepOnTileService::class.java))
    }

    fun setApplicationAsStopped() {
        preferences.setAppIsLaunched(false)
        updateQSTile()
        clearShortcuts()
    }

    fun manageAppShortcuts() {
        application.sendBroadcast(manageShortcutsIntent)
    }

    fun createShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (this::manageShortcutJob.isInitialized) {
                manageShortcutJob.cancel()
            }

            manageShortcutJob = ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
                // Create dynamic list of timeout values
                val availableTimeout: ArrayList<Int> = ArrayList()
                availableTimeout.addAll(preferences.getSelectedTimeout())
                availableTimeout.remove(preferences.getOriginalTimeout())
                availableTimeout.remove(preferences.getCurrentTimeout())
                availableTimeout.add(-42)
                if (preferences.getPreviousValue() != 0) availableTimeout.add(-43)
                availableTimeout.sort()

                // Build list of shortcuts
                shortcutManager?.let {
                    if (availableTimeout.size > 2) {
                        // Remove all previous shortcuts
                        it.removeAllDynamicShortcuts()

                        // Filter to get only the 5 first shortcuts
                        for (timeout in availableTimeout.take(5)) {
                            // Create shortcut
                            val shortcut = ShortcutInfo.Builder(application, timeout.toString())
                                .setShortLabel(getDisplayTimeout(timeout))
                                .setIntent(shortcutIntent.putExtra("timeout", timeout))
                            timeoutMap[timeout]?.let { label -> shortcut.setLongLabel(application.getString(label)) }

                            // Call Glide to set icon and build shortcut
                            setShortcutsIconWithGlide(timeout, shortcut)
                        }
                    }
                }
                availableTimeout.clear()
            }
        }
    }

    fun getTimeoutIconData(iconTimeout: Int, iconSize: Int): TimeoutIconData {
        return TimeoutIconData(
            iconTimeout,
            iconSize,
            preferences.getQSStyleFontSize(),
            preferences.getQSStyleFontSkew(),
            preferences.getQSStyleFontSpacing(),
            preferences.getQSStyleTypefaceSansSerif(),
            preferences.getQSStyleTypefaceSerif(),
            preferences.getQSStyleTypefaceMonospace(),
            preferences.getQSStyleFontBold(),
            preferences.getQSStyleFontUnderline(),
            preferences.getQSStyleFontSMCP(),
            preferences.getQSStyleTextFill(),
            preferences.getQSStyleTextFillStroke(),
            preferences.getQSStyleTextStroke()
        )
    }

    fun getDisplayTimeout(screenOffTimeout: Int): String {
        stringTimeout = when {
            screenOffTimeout == Int.MAX_VALUE ->
                application.getString(R.string.qs_short_infinite)
            screenOffTimeout >= 3600000 ->
                StringBuilder((screenOffTimeout / 3600000).toString())
                    .append(application.getString(R.string.qs_short_hour))
                    .toString()
            screenOffTimeout >= 60000 ->
                StringBuilder((screenOffTimeout / 60000).toString())
                    .append(application.getString(R.string.qs_short_minute))
                    .toString()
            screenOffTimeout == -42 ->
                application.getString(R.string.timeout_restore_short)
            screenOffTimeout == -43 ->
                application.getString(R.string.timeout_previous_short)
            else ->
                StringBuilder((screenOffTimeout / 1000).toString())
                    .append(application.getString(R.string.qs_short_second))
                    .toString()
        }
        return stringTimeout
    }

    fun getDisplayTimeoutArray(): ArrayMap<Int, Int> {
        return timeoutMap
    }

    private fun setShortcutsIconWithGlide(timeout: Int, shortcutInfo: ShortcutInfo.Builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // Set real value for special timeout (current and previous)
            newTimeout = when (timeout) {
                -42 -> {
                    preferences.getOriginalTimeout()
                }
                -43 -> {
                    preferences.getPreviousValue()
                }
                else -> timeout
            }

            // Create bitmap and build shortcut
            glideApp
                .asBitmap()
                .priority(Priority.LOW)
                .load(getTimeoutIconData(newTimeout, 3))
                .into(object : CustomTarget<Bitmap>(25.px, 25.px) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Set bitmap to shortcut icon
                        shortcutInfo.setIcon(Icon.createWithBitmap(resource))
                        // Build and assign shortcut
                        shortcutManager?.let {
                            if (!it.isRateLimitingActive) {
                                it.addDynamicShortcuts(listOf(shortcutInfo.build()))
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun clearShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.removeAllDynamicShortcuts()
        }
    }

    companion object {
        const val ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE = "ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE"
        const val ACTION_SHORTCUT_SET_TIMEOUT = "ACTION_SHORTCUT_SET_TIMEOUT"
        const val ACTION_MANAGE_SHORTCUTS = "ACTION_MANAGE_SHORTCUTS"

        const val ACTION_MAIN_ACTIVITY_UPDATE_UI = "ACTION_MAIN_ACTIVITY_UPDATE_UI"
        const val ACTION_MAIN_ACTIVITY_MISSING_SETTINGS = "ACTION_MAIN_ACTIVITY_MISSING_SETTINGS"
    }
}
