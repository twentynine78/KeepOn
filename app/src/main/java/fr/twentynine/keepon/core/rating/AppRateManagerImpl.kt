package fr.twentynine.keepon.core.rating

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.AppRateManager
import javax.inject.Inject

/**
 * Drives the "rate the app" flow against the Play Store: opens the store listing, reports whether a
 * handler for the market intent exists (so the rate tip is hidden on devices without it), and exposes
 * the install time used to gate when the tip first appears.
 */
class AppRateManagerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : AppRateManager {

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

    @SuppressLint("QueryPermissionsNeeded")
    private fun canOpenIntent(intent: Intent): Boolean {
        return packageManager
            .queryIntentActivities(intent, 0).isNotEmpty()
    }
}
