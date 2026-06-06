package fr.twentynine.keepon.util.timeout

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.model.ScreenTimeout
import javax.inject.Inject

interface SystemScreenTimeoutController {
    fun getSystemScreenTimeout(): ScreenTimeout
    fun setSystemScreenTimeout(timeout: ScreenTimeout)
}

class SystemScreenTimeoutControllerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : SystemScreenTimeoutController {

    private val contentResolver by lazy { context.contentResolver }

    override fun getSystemScreenTimeout(): ScreenTimeout {
        return ScreenTimeout(
            Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                DEFAULT_SCREEN_TIMEOUT
            )
        )
    }

    override fun setSystemScreenTimeout(timeout: ScreenTimeout) {
        val success = Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            timeout.value
        )
        if (!success) {
            throw SecurityException("Failed to write SCREEN_OFF_TIMEOUT — WRITE_SETTINGS permission may be missing or revoked")
        }
    }

    companion object {
        private const val DEFAULT_SCREEN_TIMEOUT = 60000
    }
}
