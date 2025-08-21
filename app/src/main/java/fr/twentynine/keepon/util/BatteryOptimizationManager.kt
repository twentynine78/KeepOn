package fr.twentynine.keepon.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface BatteryOptimizationManager {
    val batteryIsNotOptimized: Flow<Boolean>
    fun checkBatteryOptimizationState()
    fun requestDisableBatteryOptimization()
    fun isBatteryNotOptimized(): Boolean

    companion object {
        fun isBatteryNotOptimized(context: Context): Boolean {
            return BatteryOptimizationManagerImpl(context).isBatteryNotOptimized()
        }
    }
}

class BatteryOptimizationManagerImpl @Inject constructor(
    @param:ActivityContext private val context: Context
) : BatteryOptimizationManager {

    private val packageName by lazy { context.packageName }

    private val packageManager by lazy { context.getSystemService(PowerManager::class.java) }

    private val _batteryIsNotOptimized = MutableStateFlow(false)
    override val batteryIsNotOptimized = _batteryIsNotOptimized.distinctUntilChanged { old, new -> old == new }

    override fun checkBatteryOptimizationState() {
        _batteryIsNotOptimized.update { isBatteryNotOptimized() }
    }

    override fun isBatteryNotOptimized(): Boolean {
        return packageManager.isIgnoringBatteryOptimizations(packageName)
    }

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
