package fr.twentynine.keepon.core.permission

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import javax.inject.Inject

/** Requests the WRITE_SETTINGS permission, which the app needs to change the system screen timeout. */
interface SystemSettingPermissionManager {
    fun requestWriteSystemSettingsPermission()
}

/**
 * Activity-scoped impl: opens the "Modify system settings" screen, then polls (up to ~60s) for the
 * permission to be granted and bounces the activity back to the foreground as soon as it is, since
 * that system screen gives no result callback.
 */
class SystemSettingPermissionManagerImpl @Inject constructor(@param:ActivityContext private val context: Context) : SystemSettingPermissionManager {

    private var checkPermissionJob: Job? = null

    private val waitTime = 200.milliseconds
    private val maxCheckRepeat = 300

    override fun requestWriteSystemSettingsPermission() {
        val permissionIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            .setData(("package:" + context.packageName).toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

        context.startActivity(permissionIntent)
        checkPermission()
    }

    private fun checkPermission() {
        checkPermissionJob?.cancel()

        if (context is ComponentActivity) {
            val restartActivityIntent = context.intent
                .addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            checkPermissionJob = context.lifecycleScope.launch(Dispatchers.Default) {
                repeat(maxCheckRepeat) {
                    if (Settings.System.canWrite(context)) {
                        context.startActivity(restartActivityIntent)
                        return@launch
                    }
                    delay(waitTime)
                }
            }
        }
    }
}
