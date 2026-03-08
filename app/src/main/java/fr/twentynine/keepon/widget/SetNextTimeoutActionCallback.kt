package fr.twentynine.keepon.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.di.SetNextTimeoutActionCallbackEntryPoint
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
        val userPreferencesRepository = hiltEntryPoint.userPreferencesRepository()
        val appComponentsUpdater = hiltEntryPoint.appComponentsUpdater()

        val currentTimeoutValue: Int = parameters[currentTimeoutParameterKey] ?: return
        val currentTimeout = ScreenTimeout(currentTimeoutValue)

        withContext(Dispatchers.IO) {
            userPreferencesRepository.setNextSelectedSystemScreenTimeout(currentTimeout) {
                appComponentsUpdater.requestUpdate()
            }
        }
    }

    companion object {
        val currentTimeoutParameterKey = ActionParameters.Key<Int>(
            "CURRENT_TIMEOUT"
        )
    }
}
