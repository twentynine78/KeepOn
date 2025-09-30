package fr.twentynine.keepon.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

interface AppRateHelper {
    fun openPlayStore()
    fun canRateApp(): Boolean
    fun getFirstInstallTime(): Long
    fun needShowRateTip(
        currentCount: Long,
        firstInstallTime: Long,
        canRateApp: Boolean,
    ): Boolean
}

class AppRateHelperImpl @Inject constructor(@param:ApplicationContext private val context: Context) : AppRateHelper {

    private val packageName by lazy { context.packageName }
    private val packageManager by lazy { context.packageManager }

    private fun getPlayStoreIntent(): Intent {
        val uri = "market://details?id=$packageName".toUri()
        return Intent(Intent.ACTION_VIEW, uri)
    }

    override fun openPlayStore() {
        val intent = getPlayStoreIntent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    override fun canRateApp(): Boolean {
        return canOpenIntent(getPlayStoreIntent())
    }

    override fun getFirstInstallTime(): Long {
        return packageManager.getPackageInfo(packageName, 0).firstInstallTime
    }

    private fun getRemainingCount(currentCount: Long): Long {
        return if (currentCount < DEFAULT_COUNT) {
            DEFAULT_COUNT - currentCount
        } else {
            0
        }
    }

    override fun needShowRateTip(
        currentCount: Long,
        firstInstallTime: Long,
        canRateApp: Boolean,
    ): Boolean {
        val shouldShowRequest = (
            getRemainingCount(currentCount) == 0L &&
                Calendar.getInstance(
                    TimeZone.getTimeZone("utc")
                ).timeInMillis > (firstInstallTime + DEFAULT_INSTALL_TIME)
            )

        return shouldShowRequest && canRateApp
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun canOpenIntent(intent: Intent): Boolean {
        return packageManager
            .queryIntentActivities(intent, 0).isNotEmpty()
    }

    companion object {
        private const val DEFAULT_COUNT = 10
        private const val DEFAULT_INSTALL_TIME = (3 * (1000 * 60 * 60 * 24)).toLong() // 3 days
    }
}
