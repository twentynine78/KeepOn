package fr.twentynine.keepon.utils

import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
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
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.collections.ArrayList


object KeepOnUtils {
    internal const val TAG_MISSING_SETTINGS = "missing_settings"
    internal const val NOTIFICATION_CHANNEL_ID = "keepon_services"
    private const val NOTIFICATION_CHANNEL_DESC = "KeepOn Services"
    private const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_MIN

    private val Float.px: Float
        get() = (this * Resources.getSystem().displayMetrics.density)

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun getTimeoutValueArray() : ArrayList<Int> {
        return arrayListOf(
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
    }

    fun getNextTimeoutValue(context: Context): Int {
        val allTimeouts = getTimeoutValueArray()
        allTimeouts.indexOf(getCurrentTimeout(context))

        val availableTimeout: ArrayList<Int> = ArrayList()
        availableTimeout.addAll(getSelectedTimeout(context))
        availableTimeout.remove(getOriginalTimeout(context))
        availableTimeout.add(getOriginalTimeout(context))
        availableTimeout.sort()

        val currentTimeout = getCurrentTimeout(context)
        var allCurrentIndex = allTimeouts.indexOf(currentTimeout)
        for (i in 0 until allTimeouts.size) {
            if (allCurrentIndex == allTimeouts.size - 1 || allCurrentIndex == -1) {
                allCurrentIndex = 0
            } else {
                allCurrentIndex++
            }
            if (availableTimeout.indexOf(allTimeouts[allCurrentIndex]) != -1) {
                return availableTimeout[availableTimeout.indexOf(allTimeouts[allCurrentIndex])]
            }
        }
        return getCurrentTimeout(context)
    }

    fun getBitmapFromText(timeout: Int, context: Context, bigSize: Boolean = false): Bitmap {
        // Set scale ratio to 3 for small size
        val scaleRatio = if (bigSize) 1 else 3

        val imageWidth = 150.px / scaleRatio
        val imageHeight = 150.px / scaleRatio

        val displayTimeout = getDisplayTimeout(timeout, context)
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        canvas.drawColor(Color.TRANSPARENT)

        var textSize = when (displayTimeout.length) {
            1 -> 115f.px / scaleRatio
            2 -> 86f.px / scaleRatio
            3 -> 70f.px / scaleRatio
            else -> 60f.px / scaleRatio
        }
        // Set text size from saved preference
        textSize += (Preferences.getQSStyleFontSize(context) * 2).px / scaleRatio
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = Preferences.getQSStyleFontBold(context)
        val typeface: Typeface =  when {
            Preferences.getQSStyleTypefaceSansSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceMonospace(context) -> {
                if (bold) {
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                }
            }
            else -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        paint.typeface = typeface

        // Set text style from saved preference
        val paintStyle: Paint.Style = when {
            Preferences.getQSStyleTextFill(context) -> {
                Paint.Style.FILL
            }
            Preferences.getQSStyleTextFillStroke(context) -> {
                paint.strokeWidth = 3f.px / scaleRatio
                Paint.Style.FILL_AND_STROKE
            }
            Preferences.getQSStyleTextStroke(context) -> {
                paint.strokeWidth = 3f.px / scaleRatio
                Paint.Style.STROKE
            }
            else -> Paint.Style.FILL
        }
        paint.style = paintStyle

        // Set text skew from preference
        paint.textSkewX = (-(Preferences.getQSStyleFontSkew(context) / 1.7).toFloat())

        // Set font SMCP from preference
        if (Preferences.getQSStyleFontSMCP(context)) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = Preferences.getQSStyleFontUnderline(context)

        paint.textAlign = Align.LEFT
        paint.isAntiAlias = true
        paint.color = Color.WHITE

        // Calculate coordinates
        val rect = Rect()
        canvas.getClipBounds(rect)
        val canvasHeight = rect.height()
        val canvasWidth = rect.width()
        paint.getTextBounds(displayTimeout, 0, displayTimeout.length, rect)
        val x = canvasWidth / 2f - rect.width() / 2f - rect.left
        val y = canvasHeight / 2f + rect.height() / 2f - rect.bottom

        // Draw text
        canvas.drawText(
            displayTimeout,
            x + ((Preferences.getQSStyleFontSpacing(context) * 7).px / scaleRatio),
            y,
            paint
        )
        canvas.save()
        canvas.restore()

        return bitmap
    }

    fun getShortcutBitmapFromText(timeout: Int, context: Context): Bitmap {
        val imageWidth = 25.px
        val imageHeight = 25.px

        val textColor = Color.parseColor("#FF3B3B3B")
        val shadowColor = Color.parseColor("#82222222")

        val displayTimeout = getDisplayTimeout(timeout, context)

        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.setShadowLayer(2f, 1f, 1f, shadowColor)

        canvas.drawCircle(
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat(),
            11.px.toFloat(),
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.color = textColor
        paint.strokeWidth = 2f
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawCircle(
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat(),
            11.px.toFloat(),
            paint
        )

        var textSize = when (displayTimeout.length) {
            1 -> 13f.px
            2 -> 9f.px
            3 -> 7f.px
            else -> 5f.px
        }
        // Set text size from saved preference
        textSize += (Preferences.getQSStyleFontSize(context) / 2).px
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = Preferences.getQSStyleFontBold(context)
        val typeface: Typeface =  when {
            Preferences.getQSStyleTypefaceSansSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceMonospace(context) -> {
                if (bold) {
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                }
            }
            else -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        paint.typeface = typeface

        // Set text style from saved preference
        val paintStyle: Paint.Style = when {
            Preferences.getQSStyleTextFill(context) -> {
                Paint.Style.FILL
            }
            Preferences.getQSStyleTextFillStroke(context) -> {
                paint.strokeWidth = 1f.px
                Paint.Style.FILL_AND_STROKE
            }
            Preferences.getQSStyleTextStroke(context) -> {
                paint.strokeWidth = 1f.px
                Paint.Style.STROKE
            }
            else -> Paint.Style.FILL
        }
        paint.style = paintStyle
        paint.strokeCap = Paint.Cap.ROUND

        // Set text skew from preference
        paint.textSkewX = (-(Preferences.getQSStyleFontSkew(context) / 1.7).toFloat())

        // Set font SMCP from preference
        if (Preferences.getQSStyleFontSMCP(context)) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = Preferences.getQSStyleFontUnderline(context)

        paint.textAlign = Align.LEFT
        paint.isAntiAlias = true
        paint.color = textColor
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Calculate coordinates
        val rect = Rect(0, 0, imageWidth, imageHeight)
        val canvasHeight = rect.height()
        val canvasWidth = rect.width()

        paint.setShadowLayer(1f, 1f, 1f, shadowColor)

        canvas.save()

        // If it is not restore timeout, draw text
        paint.getTextBounds(displayTimeout, 0, displayTimeout.length, rect)
        val x = canvasWidth / 2f - rect.width() / 2f - rect.left
        val y = canvasHeight / 2f + rect.height() / 2f - rect.bottom

        // Draw text
        canvas.drawText(
            displayTimeout,
            x + (Preferences.getQSStyleFontSpacing(context) / 2).px,
            y,
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
        val origTimeout = Preferences.getOriginalTimeout(context)
        return if (origTimeout == 0)
            getCurrentTimeout(context)
        else
            origTimeout
    }

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

    fun stopScreenTimeoutObserverService(context: Context) {
        val broadcastIntent = Intent(
            context.applicationContext,
            ServicesManagerReceiver::class.java
        )
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
        fun checkSettings() {
            runBlocking {
                if (!isNotificationEnabled(context)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(context.applicationContext, returnClass)
                        //intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    }
                } else {
                    delay(200)
                    CoroutineScope(Dispatchers.Default).launch {
                        checkSettings()
                    }
                }
            }
        }

        fun checkSettingOn() = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            withTimeout(
                60000
            ) {
                checkSettings()
            }
        }

        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(true)

        val image = dialog.findViewById(R.id.image_dialog) as ImageView
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_notification
            )
        )

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
                        //.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    checkSettingOn()
                    context.startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", context.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    //.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                checkSettingOn()
                context.startActivity(intent)
            }
        }
        dialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return dialog
    }

    fun getPermissionDialog(context: Context, returnClass: Class<*>): Dialog {
        fun checkSettings() {
            runBlocking {
                if (Settings.System.canWrite(context.applicationContext)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(context.applicationContext, returnClass)
                        //intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    }
                } else {
                    delay(200)
                    CoroutineScope(Dispatchers.Default).launch {
                        checkSettings()
                    }
                }
            }
        }

        fun checkSettingOn() = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            withTimeout(
                60000
            ) {
                checkSettings()
            }
        }

        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(false)

        val image = dialog.findViewById(R.id.image_dialog) as ImageView
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_permission
            )
        )

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
                //.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            checkSettingOn()
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
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_missing
            )
        )

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

    fun getDefaultTimeoutDialog(timeout: Int, timeoutText: String, context: Context): Dialog {
        val dialog = Dialog(context, R.style.DialogStyle)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(true)

        val image = dialog.findViewById(R.id.image_dialog) as ImageView
        image.setImageBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.mipmap.dialog_logo_default
            )
        )

        val text = dialog.findViewById(R.id.text_dialog) as TextView
        text.text = String.format(
            Locale.getDefault(),
            context.getString(R.string.dialog_default_timeout_text),
            timeoutText.toLowerCase(
                Locale.getDefault()
            )
        )

        val button = dialog.findViewById(R.id.btn_dialog) as Button
        button.text = context.getString(R.string.dialog_default_timeout_button)
        button.setOnClickListener {
            val previousOriginalTimeout = getOriginalTimeout(context)
            updateOriginalTimeout(timeout, context)
            if (getCurrentTimeout(context) == previousOriginalTimeout) {
                setTimeout(timeout, context)
            }
            sendBroadcastUpdateMainUI(context)
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

        val button = dialog.findViewById(R.id.btn_close) as Button
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

        val button = dialog.findViewById(R.id.btn_close) as Button
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
        notification.setContentTitle(
            String.format(
                Locale.getDefault(), "%s - %s", context.getString(
                    R.string.app_name
                ), contentText
            )
        )
        notification.setContentText(context.getString(R.string.notification_hide))
        notification.setSmallIcon(R.mipmap.ic_qs_keepon)

        var hideIntent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!TextUtils.isEmpty(NOTIFICATION_CHANNEL_ID)) {
                hideIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    //.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        } else {
            val uri = Uri.fromParts("package", context.packageName, null)
            hideIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                //.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(hideIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notification.setContentIntent(pendingIntent)

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

    fun getKeepOnState(context: Context): Boolean {
        return getCurrentTimeout(context) != getOriginalTimeout(context)
    }

    fun setTimeout(timeout: Int, context: Context) {
        try {
            setValueChange(true, context)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, timeout
            )
        } catch (e: Exception) {
            setValueChange(false, context)
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

    fun setNewTimeout(value: Int, context: Context) {
        Preferences.setNewValue(value, context)
    }

    fun getNewTimeout(context: Context): Int {
        return Preferences.getNewValue(context)
    }

    fun setPreviousTimeout(value: Int, context: Context) {
        Preferences.setPreviousValue(value, context)
    }

    fun getPreviousTimeout(context: Context): Int {
        return Preferences.getPreviousValue(context)
    }

    fun setSelectedTimeout(selectedList: ArrayList<Int>, context: Context) {
        Preferences.setSelectedTimeout(selectedList, context)
    }

    fun getSelectedTimeout(context: Context): ArrayList<Int> {
        return Preferences.getSelectedTimeout(context)
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

    fun manageAppShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // Define map with long text
            val timeoutMap: HashMap<Int, Int> = hashMapOf(
                -43 to R.string.timeout_previous,
                -42 to R.string.timeout_restore,
                getTimeoutValueArray()[0] to R.string.timeout_15_seconds,
                getTimeoutValueArray()[1] to R.string.timeout_30_seconds,
                getTimeoutValueArray()[2] to R.string.timeout_1_minute,
                getTimeoutValueArray()[3] to R.string.timeout_2_minutes,
                getTimeoutValueArray()[4] to R.string.timeout_5_minutes,
                getTimeoutValueArray()[5] to R.string.timeout_10_minutes,
                getTimeoutValueArray()[6] to R.string.timeout_30_minutes,
                getTimeoutValueArray()[7] to R.string.timeout_1_hour,
                getTimeoutValueArray()[8] to R.string.timeout_infinite
            )

            // Create dynamic list of timeout values
            val availableTimeout: ArrayList<Int> = ArrayList()
            availableTimeout.addAll(getSelectedTimeout(context))
            availableTimeout.remove(getOriginalTimeout(context))
            availableTimeout.remove(getCurrentTimeout(context))
            availableTimeout.add(-42)
            if (getPreviousTimeout(context) != 0) availableTimeout.add(-43)
            availableTimeout.sort()

            // Build list of shortcuts
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)

            if (shortcutManager != null) {
                // Remove all previous shortcuts
                shortcutManager.removeAllDynamicShortcuts()

                // Filter to get only the 5 first shortcuts
                for (timeout in availableTimeout.take(5)) {
                    // Create Intent
                    val intent = Intent(context.applicationContext, SplashScreen::class.java)
                    intent.action = ServicesManagerReceiver.ACTION_SET_TIMEOUT
                    intent.putExtra("timeout", timeout)

                    // Create shortcut
                    val shortcut = ShortcutInfo.Builder(context, String.format(Locale.getDefault(), "%d", timeout))
                        .setShortLabel(getDisplayTimeout(timeout, context))
                        .setLongLabel(context.getString(timeoutMap[timeout]!!))
                        .setIntent(intent)

                    // Call Glider to set icon and build shortcut
                    setShortcutsIconWithGlide(timeout, shortcut, context)
                }
            }
        }
    }

    fun getIconStyleSignature(context: Context): String {
        return String.format(
            Locale.getDefault(),
            "%d,%d,%d,%b,%b,%b,%b,%b,%b,%b,%b,%b",
            Preferences.getQSStyleFontSize(context),
            Preferences.getQSStyleFontSkew(context),
            Preferences.getQSStyleFontSpacing(context),
            Preferences.getQSStyleTypefaceSansSerif(context),
            Preferences.getQSStyleTypefaceSerif(context),
            Preferences.getQSStyleTypefaceMonospace(context),
            Preferences.getQSStyleFontBold(context),
            Preferences.getQSStyleFontUnderline(context),
            Preferences.getQSStyleFontSMCP(context),
            Preferences.getQSStyleTextFill(context),
            Preferences.getQSStyleTextFillStroke(context),
            Preferences.getQSStyleTextStroke(context)
        )
    }

    private fun setShortcutsIconWithGlide(timeout: Int, shortcutInfo: ShortcutInfo.Builder, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val newTimeout = when (timeout) {
                -42 -> {
                    getOriginalTimeout(context)
                }
                -43 -> {
                    getPreviousTimeout(context)
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
            screenOffTimeout == -42 -> String.format(
                Locale.getDefault(),
                "%s",
                context.getString(R.string.timeout_restore_short)
            )
            screenOffTimeout == -43 -> String.format(
                Locale.getDefault(),
                "%s",
                context.getString(R.string.timeout_previous_short)
            )
            else -> String.format(
                Locale.getDefault(),
                "%d%s",
                screenOffTimeout / 1000,
                context.getString(R.string.qs_short_second)
            )
        }
    }

    private fun updateOriginalTimeout(newTimeout: Int, context: Context) {
        Preferences.setOriginalTimeout(newTimeout, context)
    }
}