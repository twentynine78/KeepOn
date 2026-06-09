package fr.twentynine.keepon.core.permission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

/** Requests the user to exempt the app from battery optimization (needed for reliable background work). */
interface BatteryOptimizationManager {
    fun requestDisableBatteryOptimization()
}

/** Opens the system "ignore battery optimizations" request for this package. Activity-scoped. */
class BatteryOptimizationManagerImpl @Inject constructor(
    @param:ActivityContext private val context: Context
) : BatteryOptimizationManager {

    @SuppressLint("BatteryLife")
    override fun requestDisableBatteryOptimization() {
        val batteryOptimizationIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(("package:" + context.packageName).toUri())
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

        context.startActivity(batteryOptimizationIntent)
    }
}
