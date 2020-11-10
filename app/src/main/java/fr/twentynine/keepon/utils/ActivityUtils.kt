package fr.twentynine.keepon.utils

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ActivityScope
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import java.util.Formatter
import java.util.Locale

@ActivityScope
@InjectConstructor
class ActivityUtils(private val activity: AppCompatActivity) {

    private val preferences: Preferences by lazy()

    private val mNotificationDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_custom)
        value.setCancelable(true)

        // Set ImageView
        value.findViewById<ImageView>(R.id.image_dialog).setImageBitmap(BitmapFactory.decodeResource(activity.resources, R.mipmap.dialog_logo_notification))
        // Set TextView
        value.findViewById<TextView>(R.id.text_dialog).text = activity.getString(R.string.dialog_notification_text)
        // Set Button
        val button = value.findViewById<Button>(R.id.btn_dialog)
        button.text = activity.getString(R.string.dialog_notification_button)
        button.setOnClickListener {
            value.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(ServiceUtils.NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, ServiceUtils.NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    checkNotification()
                    activity.startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", activity.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                checkNotification()
                activity.startActivity(intent)
            }
        }
        // Set background transparent
        value.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))
    }
    private val mPermissionDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_custom)
        value.setCancelable(false)

        // Set ImageView
        value.findViewById<ImageView>(R.id.image_dialog).setImageBitmap(BitmapFactory.decodeResource(activity.resources, R.mipmap.dialog_logo_permission))
        // Set TextView
        value.findViewById<TextView>(R.id.text_dialog).text = activity.getString(R.string.dialog_permission_text)
        // Set Button
        val button = value.findViewById<Button>(R.id.btn_dialog)
        button.text = activity.getString(R.string.dialog_permission_button)
        button.setOnClickListener {
            value.dismiss()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + activity.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            checkPermission()
            activity.startActivity(intent)
        }
        // Set background transparent
        value.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))
    }
    private val mMissingSettingsDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_custom)
        value.setCancelable(true)

        // Set ImageView
        value.findViewById<ImageView>(R.id.image_dialog).setImageBitmap(BitmapFactory.decodeResource(activity.resources, R.mipmap.dialog_logo_missing))
        // Set TextView
        value.findViewById<TextView>(R.id.text_dialog).text = activity.getString(R.string.dialog_missing_settings_text)
        // Set Button
        val button = value.findViewById<Button>(R.id.btn_dialog)
        button.text = activity.getString(R.string.dialog_missing_settings_button)
        button.setOnClickListener {
            value.dismiss()
        }
        // Set background transparent
        value.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))
    }
    private val mDefaultTimeoutDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_custom)
        value.setCancelable(true)

        // Set Image
        value.findViewById<ImageView>(R.id.image_dialog).setImageBitmap(BitmapFactory.decodeResource(activity.resources, R.mipmap.dialog_logo_default))
        // Set Button text
        value.findViewById<Button>(R.id.btn_dialog).text = activity.getString(R.string.dialog_default_timeout_button)
    }
    private val mCreditsDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_credits)
        value.setCancelable(true)

        // Set Button action
        value.findViewById<Button>(R.id.btn_close).setOnClickListener {
            value.dismiss()
        }
        // Set background transparent
        value.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))
    }
    private val mAddQSTileDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_add_qstile)
        value.setCancelable(true)

        // Set Button action
        value.findViewById<Button>(R.id.btn_close).setOnClickListener {
            value.dismiss()
        }
        // Set background transparent
        value.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))
    }
    private var checkPermissionJob: Job? = null
    private var checkNotificationJob: Job? = null

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    fun isNotificationEnabled(): Boolean {
        var enabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(ServiceUtils.NOTIFICATION_CHANNEL_ID)
            if (channel != null) { enabled = (channel.importance != NotificationManager.IMPORTANCE_NONE) }
        } else {
            enabled = NotificationManagerCompat.from(activity).areNotificationsEnabled()
        }
        return enabled
    }

    fun getNotificationDialog(): Dialog {
        return mNotificationDialog
    }

    fun getPermissionDialog(): Dialog {
        return mPermissionDialog
    }

    fun getMissingSettingsDialog(): Dialog {
        return mMissingSettingsDialog
    }

    fun getDefaultTimeoutDialog(timeout: Int, timeoutText: String): Dialog {
        // Set Text
        mDefaultTimeoutDialog.findViewById<TextView>(R.id.text_dialog)
            .text = Formatter().format(
                activity.getString(R.string.dialog_default_timeout_text),
                timeoutText.toLowerCase(Locale.getDefault())
            ).toString()

        // Set Button action
        mDefaultTimeoutDialog.findViewById<Button>(R.id.btn_dialog)
            .setOnClickListener {
                preferences.setOriginalTimeout(timeout)
                preferences.setTimeout(timeout)

                mDefaultTimeoutDialog.dismiss()
            }

        // Set background transparent
        mDefaultTimeoutDialog.window?.setBackgroundDrawable((ColorDrawable(Color.TRANSPARENT)))

        return mDefaultTimeoutDialog
    }

    fun getCreditsDialog(): Dialog {
        return mCreditsDialog
    }

    fun getAddQSTileDialog(): Dialog {
        return mAddQSTileDialog
    }

    fun darkerColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    fun getAppVersion(): String {
        return try {
            activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun checkNotification() {
        checkNotificationJob?.cancel()
        checkNotificationJob = activity.lifecycleScope.launch(Dispatchers.Default) {
            delay(500)
            repeat(300) {
                if (!isNotificationEnabled()) {
                    try {
                        val intent = Intent(activity.applicationContext, activity::class.java)
                        activity.startActivity(intent)
                    } finally {
                        return@launch
                    }
                } else {
                    delay(200)
                }
            }
        }
    }
    fun checkPermission() {
        checkPermissionJob?.cancel()
        checkPermissionJob = activity.lifecycleScope.launch(Dispatchers.Default) {
            delay(500)
            repeat(300) {
                if (Settings.System.canWrite(activity)) {
                    try {
                        val intent = Intent(activity.applicationContext, activity::class.java)
                        activity.startActivity(intent)
                    } finally {
                        return@launch
                    }
                } else {
                    delay(200)
                }
            }
        }
    }
}
