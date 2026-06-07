package fr.twentynine.keepon.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.di.entrypoint.SetNextTimeoutActionCallbackEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetNextTimeoutActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appContext = context.applicationContext
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            SetNextTimeoutActionCallbackEntryPoint::class.java,
        )

        withContext(Dispatchers.IO) {
            // Re-evaluate at click time (the rendered action can be stale): if the timeout
            // cannot be cycled or a required permission is missing, open the app instead,
            // mirroring the QS tile.
            val shouldRouteToAppUseCase = hiltEntryPoint.shouldRouteToAppUseCase()
            if (shouldRouteToAppUseCase()) {
                val mainActivityIntent = Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                appContext.startActivity(mainActivityIntent)
            } else {
                val currentTimeoutValue: Int = parameters[currentTimeoutParameterKey] ?: return@withContext
                val setNextSystemScreenTimeoutUseCase = hiltEntryPoint.setNextSystemScreenTimeoutUseCase()
                setNextSystemScreenTimeoutUseCase(ScreenTimeout(currentTimeoutValue))
            }
        }
    }

    companion object {
        val currentTimeoutParameterKey = ActionParameters.Key<Int>(
            "CURRENT_TIMEOUT"
        )
    }
}
