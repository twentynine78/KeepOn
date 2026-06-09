package fr.twentynine.keepon.core.component

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.AddTileServiceManager
import fr.twentynine.keepon.services.KeepOnTileService
import java.util.concurrent.Executor
import javax.inject.Inject

/**
 * Prompts the user (Android 13+) to add the KeepOn quick-settings tile via [StatusBarManager],
 * reporting the outcome through the success/error callbacks. A no-op on older versions, where tiles
 * can only be added manually.
 */
class AddTileServiceManagerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : AddTileServiceManager {

    override fun requestAddTileService(successCallback: () -> Unit, errorCallback: (Int) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager =
                context.getSystemService(StatusBarManager::class.java)

            val resultSuccessExecutor = Executor {
                successCallback()
            }

            statusBarManager.requestAddTileService(
                ComponentName(context, KeepOnTileService::class.java),
                context.getString(R.string.qs_service_name),
                Icon.createWithResource(context, R.drawable.ic_keepon),
                resultSuccessExecutor,
                errorCallback
            )
        }
    }
}
