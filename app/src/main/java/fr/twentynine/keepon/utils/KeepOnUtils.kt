package fr.twentynine.keepon.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Paint.Align
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.preferences.Preferences
import java.util.*


object KeepOnUtils {

    internal const val TAG_MISSING_SETTINGS = "missing_settings"
    internal const val NOTIFICATION_CHANNEL_ID = "keepon_services"
    private const val NOTIFICATION_CHANNEL_DESC = "KeepOn Services"
    private const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_MIN

    private fun getDisplayTimeout(screenOffTimeout: Int, context: Context): String {
        val returnDisplayTimeout by lazy {
            when {
                screenOffTimeout == Int.MAX_VALUE -> String.format(
                    Locale.getDefault(),
                    context.getString(R.string.qs_short_infinite)
                )
                screenOffTimeout >= 3600000 -> String.format(
                    Locale.getDefault(),
                    "%d%s",
                    screenOffTimeout / 3600000,
                    context.getString(R.string.qs_short_hour)
                )
                screenOffTimeout >= 60000 -> String.format(
                    Locale.getDefault(),
                    "%d%s",
                    screenOffTimeout / 60000,
                    context.getString(R.string.qs_short_minute)
                )
                else -> String.format(
                    Locale.getDefault(),
                    "%d%s",
                    screenOffTimeout / 1000,
                    context.getString(R.string.qs_short_second)
                )
            }
        }
        return returnDisplayTimeout
    }

    @JvmStatic fun getBitmapFromText(timeout: Int, context: Context): Bitmap {
        val returnBitmap by lazy {
            val imageWidth = 250
            val imageHeight = 200

            val displayTimeout = getDisplayTimeout(timeout, context)
            val textSize = when (displayTimeout.length) {
                1 -> 170f
                2 -> 140f
                3 -> 120f
                else -> 100f
            }

            val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT)
            val paint by lazy {
                Paint()
            }
            paint.textAlign = Align.CENTER
            paint.isAntiAlias = true
            paint.textSize = textSize
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = Color.WHITE

            canvas.drawText(
                displayTimeout,
                ((paint.strokeWidth / 2) + (imageWidth / 2)),
                ((imageHeight / 2) + (textSize / 3)),
                paint
            )
            canvas.save()
            canvas.restore()

            bitmap
        }
        return returnBitmap
    }

    @JvmStatic fun updateOriginalTimeout(context: Context) {
        Preferences.setOriginalTimeout(getCurrentTimeout(context), context)
    }

    @JvmStatic fun getOriginalTimeout(context: Context): Int {
        val returnOriginalTimeout by lazy {
            if (!getKeepOn(context)) {
                val originalTimeout = getCurrentTimeout(context)
                Preferences.setOriginalTimeout(originalTimeout, context)
                originalTimeout
            } else {
                Preferences.getOriginalTimeout(context)
            }
        }
        return returnOriginalTimeout
    }

    @JvmStatic fun startScreenOffReceiverService(context: Context) {
        val broadcastIntent = Intent(context, ServicesManagerReceiver::class.java)
        broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    @JvmStatic fun stopScreenOffReceiverService(context: Context) {
        val broadcastIntent = Intent(context, ServicesManagerReceiver::class.java)
        broadcastIntent.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    @JvmStatic fun startScreenTimeoutObserverService(context: Context) {
        val broadcastIntent = Intent(context, ServicesManagerReceiver::class.java)
        broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_TIMEOUT_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    @JvmStatic fun stopScreenTimeoutObserverService(context: Context) {
        val broadcastIntent = Intent(context, ServicesManagerReceiver::class.java)
        broadcastIntent.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE
        context.sendBroadcast(broadcastIntent)
    }

    @Suppress("DEPRECATION")
    @JvmStatic fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val returnIsServiceRunning by lazy {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            var running = false
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className)
                    running = true
            }
            running
        }
        return returnIsServiceRunning
    }

    @JvmStatic fun isNotificationEnabled(context: Context): Boolean {
        val returnIsServiceRunning by lazy {
            var enabled = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
                if (channel != null) enabled = (channel.importance != NotificationManager.IMPORTANCE_NONE)
            } else {
                enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
            enabled
        }
        return returnIsServiceRunning
    }

    @JvmStatic fun getNotificationDialog(context: Context): Dialog {
        val notificationDialog by lazy {
            val dialog = Dialog(context, R.style.StyleDialog)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_custom)
            dialog.setCancelable(true)

            val image = dialog.findViewById(R.id.image_dialog) as ImageView
            image.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.mipmap.dialog_logo_notification))

            val text = dialog.findViewById(R.id.text_dialog) as TextView
            text.text = context.getString(R.string.dialog_notification_text)

            val button = dialog.findViewById(R.id.btn_dialog) as Button
            button.text = context.getString(R.string.dialog_notification_button)
            button.setOnClickListener {
                dialog.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!TextUtils.isEmpty(NOTIFICATION_CHANNEL_ID)) {
                        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        context.startActivity(intent)
                    }
                } else {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    context.startActivity(intent)
                }
            }
            dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

            dialog
        }
        return notificationDialog
    }

    @JvmStatic fun getPermissionDialog(context: Context): Dialog {
        val permissionDialog by lazy {
            val dialog = Dialog(context, R.style.StyleDialog)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_custom)
            dialog.setCancelable(false)

            val image = dialog.findViewById(R.id.image_dialog) as ImageView
            image.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.mipmap.dialog_logo_permission))

            val text = dialog.findViewById(R.id.text_dialog) as TextView
            text.text = context.getString(R.string.dialog_permission_text)

            val button = dialog.findViewById(R.id.btn_dialog) as Button
            button.text = context.getString(R.string.dialog_permission_button)
            button.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(Uri.parse("package:" + context.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                context.startActivity(intent)
            }
            dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

            dialog
        }
        return permissionDialog
    }

    @JvmStatic fun getMissingSettingsDialog(context: Context): Dialog {
        val missingSettingsDialog by lazy {
            val dialog = Dialog(context, R.style.StyleDialog)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_custom)
            dialog.setCancelable(true)

            val image = dialog.findViewById(R.id.image_dialog) as ImageView
            image.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.mipmap.dialog_logo_missing))

            val text = dialog.findViewById(R.id.text_dialog) as TextView
            text.text = context.getString(R.string.dialog_missing_settings_text)

            val button = dialog.findViewById(R.id.btn_dialog) as Button
            button.text = context.getString(R.string.dialog_missing_settings_button)
            button.setOnClickListener {
                dialog.dismiss()
            }
            dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

            dialog
        }
        return missingSettingsDialog
    }

    @JvmStatic fun buildNotification(context: Context, contentText: String): Notification {
        val returnNotification by lazy {
            val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val chanID = NOTIFICATION_CHANNEL_ID
                val channel = manager.getNotificationChannel(chanID)
                if (channel == null) {
                    val chanDesc = NOTIFICATION_CHANNEL_DESC
                    val chan = NotificationChannel(chanID, chanDesc, NOTIFICATION_CHANNEL_IMPORTANCE)
                    val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    service.createNotificationChannel(chan)
                }
                chanID
            } else {
                ""
            }
            val notification = NotificationCompat.Builder(context, channelId)
            notification.setContentTitle(context.getString(R.string.app_name))
            notification.setContentText(contentText)
            notification.setSmallIcon(R.mipmap.ic_qs_keepon)
            notification.build()
        }
        return returnNotification
    }

    @JvmStatic fun sendBroadcastUpdateMainUI(context: Context) {
        val updateIntent = Intent()
        updateIntent.action = MainActivity.ACTION_UPDATE_UI
        context.sendBroadcast(updateIntent)
    }

    @JvmStatic fun sendBroadcastMissingSettings(context: Context) {
        val missingIntent = Intent()
        missingIntent.action = MainActivity.ACTION_MISSING_SETTINGS
        context.sendBroadcast(missingIntent)
    }

    @JvmStatic fun darkerColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    @JvmStatic fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic fun setTimeout(timeout: Int, context: Context) {
        setValueChange(true, context)
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, timeout
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic fun getCurrentTimeout(context: Context): Int {
        val returnCurrentTimeout by lazy {
            val defaultTimeout = 60000
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, defaultTimeout
            )
        }
        return returnCurrentTimeout
    }

    @JvmStatic fun setSelectedTimeout(selectedList: ArrayList<Int>, context: Context) {
        Preferences.setSelectedTimeout(selectedList, context)
    }

    @JvmStatic fun getSelectedTimeout(context: Context): ArrayList<Int> {
        return Preferences.getSelectedTimeout(context)
    }

    @JvmStatic fun getKeepOn(context: Context): Boolean {
        return Preferences.getKeepOn(context)
    }

    @JvmStatic fun setKeepOn(value: Boolean, context: Context) {
        Preferences.setKeepOn(value, context)
    }

    @JvmStatic fun getResetOnScreenOff(context: Context): Boolean {
        return Preferences.getResetTimeoutOnScreenOff(context)
    }

    @JvmStatic fun setResetOnScreenOff(value: Boolean, context: Context) {
        Preferences.setResetTimeoutOnScreenOff(value, context)
    }

    @JvmStatic fun getSkipIntro(context: Context): Boolean {
        return Preferences.getSkipIntro(context)
    }

    @JvmStatic fun setSkipIntro(value: Boolean, context: Context) {
        Preferences.setSkipIntro(value, context)
    }

    @JvmStatic fun getDarkTheme(context: Context): Boolean {
        return Preferences.getDarkTheme(context)
    }

    @JvmStatic fun setDarkTheme(value: Boolean, context: Context) {
        Preferences.setDarkTheme(value, context)
    }

    @JvmStatic fun getValueChange(context: Context): Boolean {
        return Preferences.getValueChange(context)
    }

    @JvmStatic fun setValueChange(value: Boolean, context: Context) {
        Preferences.setValueChange(value, context)
    }

    @JvmStatic fun getTileAdded(context: Context): Boolean {
        return Preferences.getTileAdded(context)
    }

    @JvmStatic fun setTileAdded(value: Boolean, context: Context) {
        Preferences.setTileAdded(value, context)
    }

    @JvmStatic fun getNewTimeout(context: Context): Int {
        return Preferences.getNewTimeout(context)
    }

    @JvmStatic fun setNewTimeout(value: Int, context: Context) {
        Preferences.setNewTimeout(value, context)
    }
}