package fr.twentynine.keepon.utils

import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import java.util.Locale


class KeepOnUtils {
    
    companion object {
        internal const val TAG_MISSING_SETTINGS = "missing_settings"
        internal const val NOTIFICATION_CHANNEL_ID = "keepon_services"
        private const val NOTIFICATION_CHANNEL_DESC = "KeepOn Services"
        private const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_MIN



        fun getBitmapFromText(timeout: Int, context: Context, bigSize: Boolean = false): Bitmap {
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
            val paint = Paint()

            canvas.drawColor(Color.TRANSPARENT)
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

            return bitmap
        }


        fun updateOriginalTimeout(context: Context) {
            Preferences.setOriginalTimeout(getCurrentTimeout(context), context)
        }

        fun getOriginalTimeout(context: Context): Int {
            return if (!getKeepOn(context)) {
                val originalTimeout = getCurrentTimeout(context)
                Preferences.setOriginalTimeout(originalTimeout, context)
                originalTimeout
            } else {
                Preferences.getOriginalTimeout(context)
            }
        }

        fun startScreenOffReceiverService(context: Context) {
            val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
            broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_SCREEN_OFF_SERVICE
            context.sendBroadcast(broadcastIntent)
        }

        fun stopScreenOffReceiverService(context: Context) {
            val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
            broadcastIntent.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_SCREEN_OFF_SERVICE
            context.sendBroadcast(broadcastIntent)
        }

        fun startScreenTimeoutObserverService(context: Context) {
            val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
            broadcastIntent.action = ServicesManagerReceiver.ACTION_START_FOREGROUND_TIMEOUT_SERVICE
            context.sendBroadcast(broadcastIntent)
        }

        fun stopScreenTimeoutObserverService(context: Context) {
            val broadcastIntent = Intent(context.applicationContext, ServicesManagerReceiver::class.java)
            broadcastIntent.action = ServicesManagerReceiver.ACTION_STOP_FOREGROUND_TIMEOUT_SERVICE
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
            val looper: Looper = if (Looper.myLooper() != null) Looper.myLooper()!! else Looper.getMainLooper()
            val handler = Handler(looper)
            val checkSettingOn: Runnable = object : Runnable {
                override fun run() {
                    if (!isNotificationEnabled(context)) {
                        val intent = Intent(context.applicationContext, returnClass)
                        context.startActivity(intent)
                        return
                    }
                    handler.postDelayed(this, 200)
                }
            }

            val dialog = Dialog(context, R.style.DialogStyle)
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
                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                        handler.postDelayed(checkSettingOn, 1000)
                        context.startActivity(intent)
                    }
                } else {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    handler.postDelayed(checkSettingOn, 1000)
                    context.startActivity(intent)
                }
            }
            dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

            return dialog
        }

        fun getPermissionDialog(context: Context, returnClass: Class<*>): Dialog {
            val looper: Looper = if (Looper.myLooper() != null) Looper.myLooper()!! else Looper.getMainLooper()
            val handler = Handler(looper)
            val checkSettingOn: Runnable = object : Runnable {
                override fun run() {
                    if (Settings.System.canWrite(context.applicationContext)) {
                        val intent = Intent(context.applicationContext, returnClass)
                        context.startActivity(intent)
                        return
                    }
                    handler.postDelayed(this, 200)
                }
            }

            val dialog = Dialog(context, R.style.DialogStyle)
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
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                handler.postDelayed(checkSettingOn, 1000)
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

            return dialog
        }

        fun buildNotification(context: Context, contentText: String): Notification {
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
            return notification.build()
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

        fun setTimeout(timeout: Int, context: Context) {
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

        fun getCurrentTimeout(context: Context): Int {
            val defaultTimeout = 60000
            return Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, defaultTimeout
            )
        }

        fun setSelectedTimeout(selectedList: ArrayList<Int>, context: Context) {
            Preferences.setSelectedTimeout(selectedList, context)
        }

        fun getSelectedTimeout(context: Context): ArrayList<Int> {
            return Preferences.getSelectedTimeout(context)
        }

        fun getKeepOn(context: Context): Boolean {
            return Preferences.getKeepOn(context)
        }

        fun setKeepOn(value: Boolean, context: Context) {
            Preferences.setKeepOn(value, context)
        }

        fun getResetOnScreenOff(context: Context): Boolean {
            return Preferences.getResetTimeoutOnScreenOff(context)
        }

        fun setResetOnScreenOff(value: Boolean, context: Context) {
            Preferences.setResetTimeoutOnScreenOff(value, context)
        }

        fun getSkipIntro(context: Context): Boolean {
            return Preferences.getSkipIntro(context)
        }

        fun setSkipIntro(value: Boolean, context: Context) {
            Preferences.setSkipIntro(value, context)
        }

        fun getDarkTheme(context: Context): Boolean {
            return Preferences.getDarkTheme(context)
        }

        fun setDarkTheme(value: Boolean, context: Context) {
            Preferences.setDarkTheme(value, context)
        }

        fun getValueChange(context: Context): Boolean {
            return Preferences.getValueChange(context)
        }

        fun setValueChange(value: Boolean, context: Context) {
            Preferences.setValueChange(value, context)
        }

        fun getTileAdded(context: Context): Boolean {
            return Preferences.getTileAdded(context)
        }

        fun setTileAdded(value: Boolean, context: Context) {
            Preferences.setTileAdded(value, context)
        }

        fun getNewTimeout(context: Context): Int {
            return Preferences.getNewTimeout(context)
        }

        fun setNewTimeout(value: Int, context: Context) {
            Preferences.setNewTimeout(value, context)
        }

        private fun getDisplayTimeout(screenOffTimeout: Int, context: Context): String {
            return when {
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
    }
}