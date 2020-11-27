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
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
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

        // Adjust width
        adjustDialogWidth(value)
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

        // Adjust width
        adjustDialogWidth(value)
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

        // Adjust width
        adjustDialogWidth(value)
    }
    private val mDefaultTimeoutDialog: Dialog by lazy { Dialog(activity, R.style.DialogStyle) }.apply {
        value.requestWindowFeature(Window.FEATURE_NO_TITLE)
        value.setContentView(R.layout.dialog_custom)
        value.setCancelable(true)

        // Set Image
        value.findViewById<ImageView>(R.id.image_dialog).setImageBitmap(BitmapFactory.decodeResource(activity.resources, R.mipmap.dialog_logo_default))
        // Set Button text
        value.findViewById<Button>(R.id.btn_dialog).text = activity.getString(R.string.dialog_default_timeout_button)

        // Adjust width
        adjustDialogWidth(value)
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

        // Adjust width
        adjustDialogWidth(value)
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

        // Adjust width
        adjustDialogWidth(value)
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

    fun getDefaultTimeoutDialog(): Dialog {
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

    @Suppress("DEPRECATION")
    fun setStatusBarColor(@ColorInt color: Int) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity.window.statusBarColor = color
    }

    fun setNavBarColor(@ColorInt color: Int) {
        activity.window.navigationBarColor = color
    }

    fun getSnackbarCoordinatorLayoutParams(snackbar: Snackbar, anchorView: View): CoordinatorLayout.LayoutParams {
        snackbar.anchorView = anchorView
        val layout = snackbar.view as Snackbar.SnackbarLayout
        val layoutParams = layout.layoutParams as CoordinatorLayout.LayoutParams
        val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        if (dpWidth >= 620) {
            layoutParams.width = 585.px
            layoutParams.anchorId = anchorView.id
            layoutParams.anchorGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        return layoutParams
    }

    fun getSnackbarFrameLayoutParams(snackbar: Snackbar, anchorView: View): FrameLayout.LayoutParams {
        snackbar.anchorView = anchorView
        val layout = snackbar.view as Snackbar.SnackbarLayout
        val layoutParams = layout.layoutParams as FrameLayout.LayoutParams
        val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        if (dpWidth >= 620) {
            layoutParams.width = 585.px
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        return layoutParams
    }

    private fun adjustDialogWidth(dialog: Dialog) {
        dialog.window?.let {
            val layoutParams = it.attributes
            val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels / displayMetrics.density
            if (dpWidth >= 620) {
                layoutParams.width = 580.px
                layoutParams.gravity = Gravity.CENTER
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }
}
