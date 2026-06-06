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
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

interface SystemSettingPermissionManager {
    fun requestWriteSystemSettingsPermission()
}

class SystemSettingPermissionManagerImpl @Inject constructor(@param:ActivityContext private val context: Context) : SystemSettingPermissionManager {

    private var checkPermissionJob: Job? = null

    private val waitTimeInMillis = 200L
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
                withTimeout(waitTimeInMillis * maxCheckRepeat) {
                    repeat(maxCheckRepeat) {
                        if (Settings.System.canWrite(context)) {
                            try {
                                context.startActivity(restartActivityIntent)
                            } finally {
                                checkPermissionJob?.cancel()
                            }
                        } else {
                            delay(waitTimeInMillis)
                        }
                    }
                }
            }
        }
    }
}
