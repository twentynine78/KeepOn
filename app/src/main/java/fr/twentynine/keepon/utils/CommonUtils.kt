package fr.twentynine.keepon.utils

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.TileService
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.utils.glide.TimeoutIconData
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.services.ScreenTimeoutObserverService
import fr.twentynine.keepon.ui.MainActivity
import fr.twentynine.keepon.ui.SplashScreen
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import javax.inject.Singleton

@ApplicationScope
@Singleton
@InjectConstructor
class CommonUtils(private val application: Application) {

    private val preferences: Preferences by lazy()
    private val glideApp: RequestManager by lazy()

    private val updateIntent: Intent by lazy { Intent() }.apply { value.action = MainActivity.ACTION_UPDATE_UI }
    private val missingIntent: Intent by lazy { Intent() }.apply { value.action = MainActivity.ACTION_MISSING_SETTINGS }
    private val bIntentManageShortcut: Intent by lazy { Intent(application.applicationContext, ServicesManagerReceiver::class.java) }.apply {
        value.action = ServicesManagerReceiver.MANAGE_SHORTCUTS
    }
    private val bIntentStartScreenOffReceiverService: Intent by lazy { Intent(application.applicationContext, ServicesManagerReceiver::class.java) }.apply {
        value.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
    }
    private val bIntentStopScreenOffReceiverService: Intent by lazy { Intent(application.applicationContext, ServicesManagerReceiver::class.java) }.apply {
        value.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
    }
    private val bIntentStartScreenTimeoutObserverService: Intent by lazy { Intent(application.applicationContext, ServicesManagerReceiver::class.java) }.apply {
        value.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_TIMEOUT_SERVICE
    }
    private val shortcutIntent: Intent by lazy { Intent(application.applicationContext, SplashScreen::class.java) }.apply {
        value.action = ServicesManagerReceiver.ACTION_SET_TIMEOUT
    }
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
    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private lateinit var manageShortcutJob: Job
    private lateinit var stringTimeout: String

    private var checkStartScreenOffReceiverServiceJob: Job? = null
    private var checkStartScreenTimeoutObserverServiceJob: Job? = null

    private var newTimeout: Int = 0

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    fun startScreenOffReceiverService() {
        var previousJobIsActive = false
        checkStartScreenOffReceiverServiceJob?.let { previousJobIsActive = it.isActive }
        if (!ScreenOffReceiverService.isRunning && !previousJobIsActive) {
            application.sendBroadcast(bIntentStartScreenOffReceiverService)

            checkStartScreenOffReceiverService()
        }
    }

    fun stopScreenOffReceiverService() {
        checkStartScreenOffReceiverServiceJob?.cancel()
        application.sendBroadcast(bIntentStopScreenOffReceiverService)
    }

    fun startScreenTimeoutObserverService() {
        var previousJobIsActive = false
        checkStartScreenTimeoutObserverServiceJob?.let { previousJobIsActive = it.isActive }
        if (!ScreenTimeoutObserverService.isRunning && !previousJobIsActive) {
            application.sendBroadcast(bIntentStartScreenTimeoutObserverService)

            checkStartScreenTimeoutObserverService()
        }
    }

    fun sendBroadcastUpdateMainUI() {
        application.sendBroadcast(updateIntent)
    }

    fun sendBroadcastMissingSettings() {
        application.sendBroadcast(missingIntent)
    }

    fun updateQSTile(delay: Long) {
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.Default) {
            delay(delay)
            withTimeout(60000) {
                TileService.requestListeningState(application, ComponentName(application, KeepOnTileService::class.java))
            }
        }
    }

    fun manageAppShortcut() {
        application.sendBroadcast(bIntentManageShortcut)
    }

    fun createShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (this::manageShortcutJob.isInitialized) {
                manageShortcutJob.cancel()
            }

            manageShortcutJob = ProcessLifecycleOwner.get().lifecycle.coroutineScope.launch(Dispatchers.IO) {
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

    fun getIconStyleSignature(): String {
        return StringBuilder()
            .append(preferences.getQSStyleFontSize())
            .append(preferences.getQSStyleFontSkew())
            .append(preferences.getQSStyleFontSpacing())
            .append(preferences.getQSStyleTypefaceSansSerif())
            .append(preferences.getQSStyleTypefaceSerif())
            .append(preferences.getQSStyleTypefaceMonospace())
            .append(preferences.getQSStyleFontBold())
            .append(preferences.getQSStyleFontUnderline())
            .append(preferences.getQSStyleFontSMCP())
            .append(preferences.getQSStyleTextFill())
            .append(preferences.getQSStyleTextFillStroke())
            .append(preferences.getQSStyleTextStroke())
            .toString()
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
                .load(TimeoutIconData(newTimeout, 3, getIconStyleSignature()))
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

    private fun checkStartScreenOffReceiverService() {
        checkStartScreenOffReceiverServiceJob?.cancel()
        checkStartScreenOffReceiverServiceJob = ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.Default) {
            delay(2000)
            repeat(5) {
                if (!ScreenOffReceiverService.isRunning) {
                    application.sendBroadcast(bIntentStartScreenOffReceiverService)
                } else {
                    return@launch
                }
            }
        }
    }

    private fun checkStartScreenTimeoutObserverService() {
        checkStartScreenTimeoutObserverServiceJob?.cancel()
        checkStartScreenTimeoutObserverServiceJob = ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.Default) {
            delay(2000)
            repeat(5) {
                if (!ScreenTimeoutObserverService.isRunning) {
                    application.sendBroadcast(bIntentStartScreenTimeoutObserverService)
                } else {
                    return@launch
                }
            }
        }
    }
}
