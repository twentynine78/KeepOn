package fr.twentynine.keepon.utils

import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.SplashScreen
import fr.twentynine.keepon.glide.GlideApp
import fr.twentynine.keepon.glide.TimeoutIconData
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.Formatter
import java.util.Locale
import kotlin.collections.ArrayList

object KeepOnUtils {
    internal const val TAG_MISSING_SETTINGS = "missing_settings"
    internal const val NOTIFICATION_CHANNEL_ID = "keepon_services"

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun startScreenOffReceiverService(context: Context) {
        val broadcastIntent = Intent(
            context.applicationContext,
            ServicesManagerReceiver::class.java
        )
        broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    fun stopScreenOffReceiverService(context: Context) {
        val broadcastIntent = Intent(
            context.applicationContext,
            ServicesManagerReceiver::class.java
        )
        broadcastIntent.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    fun startScreenTimeoutObserverService(context: Context) {
        val broadcastIntent = Intent(
            context.applicationContext,
            ServicesManagerReceiver::class.java
        )
        broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_TIMEOUT_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    fun isNotificationEnabled(context: Context): Boolean {
        var enabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (channel != null) enabled = (channel.importance != NotificationManager.IMPORTANCE_NONE)
        } else {
            enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        return enabled
    }

    fun getNotificationDialog(context: Context, returnClass: Class<*>): Dialog {
        fun checkNotification() = CoroutineScope(Dispatchers.Default).launch {
            withContext(coroutineContext) {
                delay(500)
                repeat(300) {
                    if (!isNotificationEnabled(context)) {
                        try {
                            val intent = Intent(context.applicationContext, returnClass)
                            context.startActivity(intent)
                        } finally {
                            return@withContext
                        }
                    } else {
                        delay(200)
                    }
                }
            }
        }

        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(true)

        val image = dialog.findViewById<ImageView>(R.id.image_dialog)
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_notification
            )
        )

        val text = dialog.findViewById<TextView>(R.id.text_dialog)
        text.text = context.getString(R.string.dialog_notification_text)

        val button = dialog.findViewById<Button>(R.id.btn_dialog)
        button.text = context.getString(R.string.dialog_notification_button)
        button.setOnClickListener {
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    checkNotification()
                    context.startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", context.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                checkNotification()
                context.startActivity(intent)
            }
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getPermissionDialog(context: Context, returnClass: Class<*>): Dialog {
        fun checkPermission() = CoroutineScope(Dispatchers.Default).launch {
            withContext(coroutineContext) {
                delay(500)
                repeat(300) {
                    if (Settings.System.canWrite(context)) {
                        try {
                            val intent = Intent(context.applicationContext, returnClass)
                            context.startActivity(intent)
                        } finally {
                            return@withContext
                        }
                    } else {
                        delay(200)
                    }
                }
            }
        }

        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(false)

        val image = dialog.findViewById<ImageView>(R.id.image_dialog)
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_permission
            )
        )

        val text = dialog.findViewById<TextView>(R.id.text_dialog)
        text.text = context.getString(R.string.dialog_permission_text)

        val button = dialog.findViewById<Button>(R.id.btn_dialog)
        button.text = context.getString(R.string.dialog_permission_button)
        button.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + context.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            checkPermission()
            context.startActivity(intent)
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getMissingSettingsDialog(context: Context): Dialog {
        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(true)

        val image = dialog.findViewById<ImageView>(R.id.image_dialog)
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_missing
            )
        )

        val text = dialog.findViewById<TextView>(R.id.text_dialog)
        text.text = context.getString(R.string.dialog_missing_settings_text)

        val button = dialog.findViewById<Button>(R.id.btn_dialog)
        button.text = context.getString(R.string.dialog_missing_settings_button)
        button.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getDefaultTimeoutDialog(timeout: Int, timeoutText: String, context: Context): Dialog {
        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(true)

        val image = dialog.findViewById<ImageView>(R.id.image_dialog)
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_default
            )
        )

        val text = dialog.findViewById<TextView>(R.id.text_dialog)
        val formatter = Formatter()
        text.text = formatter.format(
            context.getString(R.string.dialog_default_timeout_text),
            timeoutText.toLowerCase(Locale.getDefault())
        ).toString()

        val button = dialog.findViewById<Button>(R.id.btn_dialog)
        button.text = context.getString(R.string.dialog_default_timeout_button)
        button.setOnClickListener {
            Preferences.setOriginalTimeout(timeout, context)
            Preferences.setTimeout(timeout, context)

            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getCreditsDialog(context: Context): Dialog {
        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_credits)
        dialog.setCancelable(true)

        val button = dialog.findViewById<Button>(R.id.btn_close)
        button.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getAddQSTileDialog(context: Context): Dialog {
        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_qstile)
        dialog.setCancelable(true)

        val button = dialog.findViewById<Button>(R.id.btn_close)
        button.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun buildNotification(context: Context, contentText: String): Notification {
        // Create the NotificationChannel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (channel == null) {
                val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
                chan.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
                notificationManager.createNotificationChannel(chan)
            }
        }

        // Create Intent to launch notification settings
        val hideIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        } else {
            val uri = Uri.fromParts("package", context.packageName, null)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, hideIntent, 0)

        // Return notification
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentText)
            .setContentText(context.getString(R.string.notification_hide))
            .setSmallIcon(R.mipmap.ic_qs_keepon)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun sendBroadcastUpdateMainUI(context: Context) {
        val updateIntent = Intent()
        updateIntent.action = MainActivity.ACTION_UPDATE_UI
        context.sendBroadcast(updateIntent)
    }

    fun sendBroadcastMissingSettings(context: Context) {
        val missingIntent = Intent()
        missingIntent.action = MainActivity.ACTION_MISSING_SETTINGS
        context.sendBroadcast(missingIntent)
    }

    fun darkerColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun manageAppShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // Define map with long text
            val timeoutMap: ArrayMap<Int, Int> = arrayMapOf(
                -43 to R.string.timeout_previous,
                -42 to R.string.timeout_restore,
                Preferences.getTimeoutValueArray()[0] to R.string.timeout_15_seconds,
                Preferences.getTimeoutValueArray()[1] to R.string.timeout_30_seconds,
                Preferences.getTimeoutValueArray()[2] to R.string.timeout_1_minute,
                Preferences.getTimeoutValueArray()[3] to R.string.timeout_2_minutes,
                Preferences.getTimeoutValueArray()[4] to R.string.timeout_5_minutes,
                Preferences.getTimeoutValueArray()[5] to R.string.timeout_10_minutes,
                Preferences.getTimeoutValueArray()[6] to R.string.timeout_30_minutes,
                Preferences.getTimeoutValueArray()[7] to R.string.timeout_1_hour,
                Preferences.getTimeoutValueArray()[8] to R.string.timeout_infinite
            )

            // Create dynamic list of timeout values
            val availableTimeout: ArrayList<Int> = ArrayList()
            availableTimeout.addAll(Preferences.getSelectedTimeout(context))
            availableTimeout.remove(Preferences.getOriginalTimeout(context))
            availableTimeout.remove(Preferences.getCurrentTimeout(context))
            availableTimeout.add(-42)
            if (Preferences.getPreviousValue(context) != 0) availableTimeout.add(-43)
            availableTimeout.sort()

            // Build list of shortcuts
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            if (shortcutManager != null && availableTimeout.size > 2) {
                // Remove all previous shortcuts
                shortcutManager.removeAllDynamicShortcuts()

                // Filter to get only the 5 first shortcuts
                for (timeout in availableTimeout.take(5)) {
                    // Create Intent
                    val intent = Intent(context.applicationContext, SplashScreen::class.java)
                    intent.action = ServicesManagerReceiver.ACTION_SET_TIMEOUT
                    intent.putExtra("timeout", timeout)

                    // Create shortcut
                    val shortcut = ShortcutInfo.Builder(context, timeout.toString())
                        .setShortLabel(getDisplayTimeout(timeout, context))
                        .setLongLabel(context.getString(timeoutMap[timeout]!!))
                        .setIntent(intent)

                    // Call Glider to set icon and build shortcut
                    setShortcutsIconWithGlide(timeout, shortcut, context)
                }
            }
            timeoutMap.clear()
        }
    }

    fun getIconStyleSignature(context: Context): String {
        return StringBuilder()
            .append(Preferences.getQSStyleFontSize(context))
            .append(Preferences.getQSStyleFontSkew(context))
            .append(Preferences.getQSStyleFontSpacing(context))
            .append(Preferences.getQSStyleTypefaceSansSerif(context))
            .append(Preferences.getQSStyleTypefaceSerif(context))
            .append(Preferences.getQSStyleTypefaceMonospace(context))
            .append(Preferences.getQSStyleFontBold(context))
            .append(Preferences.getQSStyleFontUnderline(context))
            .append(Preferences.getQSStyleFontSMCP(context))
            .append(Preferences.getQSStyleTextFill(context))
            .append(Preferences.getQSStyleTextFillStroke(context))
            .append(Preferences.getQSStyleTextStroke(context))
            .toString()
    }

    fun getDisplayTimeout(screenOffTimeout: Int, context: Context): String {
        return when {
            screenOffTimeout == Int.MAX_VALUE ->
                context.getString(R.string.qs_short_infinite)
            screenOffTimeout >= 3600000 ->
                StringBuilder((screenOffTimeout / 3600000).toString())
                    .append(context.getString(R.string.qs_short_hour))
                    .toString()
            screenOffTimeout >= 60000 ->
                StringBuilder((screenOffTimeout / 60000).toString())
                    .append(context.getString(R.string.qs_short_minute))
                    .toString()
            screenOffTimeout == -42 ->
                context.getString(R.string.timeout_restore_short)
            screenOffTimeout == -43 ->
                context.getString(R.string.timeout_previous_short)
            else ->
                StringBuilder((screenOffTimeout / 1000).toString())
                    .append(context.getString(R.string.qs_short_second))
                    .toString()
        }
    }

    private fun setShortcutsIconWithGlide(timeout: Int, shortcutInfo: ShortcutInfo.Builder, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val newTimeout = when (timeout) {
                -42 -> {
                    Preferences.getOriginalTimeout(context)
                }
                -43 -> {
                    Preferences.getPreviousValue(context)
                }
                else -> timeout
            }

            GlideApp.with(context)
                .asBitmap()
                .priority(Priority.LOW)
                .load(TimeoutIconData(newTimeout, 3, getIconStyleSignature(context)))
                .into(object : CustomTarget<Bitmap>(25.px, 25.px) {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Set bitmap to shortcut icon
                        shortcutInfo.setIcon(Icon.createWithBitmap(resource))
                        // Build and assign shortcut
                        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                        if (shortcutManager != null && !shortcutManager.isRateLimitingActive) {
                            shortcutManager.addDynamicShortcuts(listOf(shortcutInfo.build()))
                        }
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
}
