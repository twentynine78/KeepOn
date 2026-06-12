package fr.twentynine.keepon.core.system

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.UserNotifier
import javax.inject.Inject

/** Shows the user-facing [UserNotifier] messages as Toasts, posted onto the main thread. */
class UserNotifierImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UserNotifier {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun notifyScreenTimeoutNotApplied() {
        showToast(R.string.screen_timeout_change_rejected)
    }

    override fun notifyBatteryOptimizationRequestUnavailable() {
        showToast(R.string.battery_optimization_request_unavailable)
    }

    override fun notifyMissingPermission() {
        showToast(R.string.toast_missing_permission)
    }

    override fun notifyScreenOffServiceError() {
        showToast(R.string.toast_screen_off_service_error)
    }

    override fun notifyInvalidScreenTimeout() {
        showToast(R.string.toast_invalid_screen_timeout)
    }

    private fun showToast(@StringRes messageRes: Int) {
        // Toast must be posted on the main thread; this port is called from background
        // coroutines (tile, widget, worker, app).
        mainHandler.post {
            Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
        }
    }
}
