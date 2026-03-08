package fr.twentynine.keepon.data.repo

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.EntryPointAccessors
import fr.twentynine.keepon.KeepOnApplication
import fr.twentynine.keepon.data.model.WidgetUIState
import fr.twentynine.keepon.di.WidgetRepositoryEntryPoint
import fr.twentynine.keepon.util.LockableJob
import fr.twentynine.keepon.widget.KeepOnWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object WidgetRepository {
    private val mutex = Mutex()

    private var currentUpdateJob: LockableJob = LockableJob()

    private val updateWidgetJob = SupervisorJob()

    private var _currentWidgetUIState = MutableStateFlow<WidgetUIState>(WidgetUIState.Loading)
    val currentWidgetUIState = _currentWidgetUIState.asStateFlow()

    private lateinit var userPreferencesRepositoryInstance: UserPreferencesRepository

    private fun getUserPreferencesRepository(context: Context): UserPreferencesRepository {
        if (!::userPreferencesRepositoryInstance.isInitialized) {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetRepositoryEntryPoint::class.java
            )
            userPreferencesRepositoryInstance = hiltEntryPoint.userPreferencesRepository()
        }
        return userPreferencesRepositoryInstance
    }

    private suspend fun getWidgetUIState(context: Context): WidgetUIState {
        getUserPreferencesRepository(context).let { userPreferencesRepository ->
            return combine(
                userPreferencesRepository.getCurrentScreenTimeoutFlow(),
                userPreferencesRepository.getKeepOnIsActiveFlow(),
                userPreferencesRepository.getTimeoutIconStyleFlow(),
                userPreferencesRepository.getSelectedScreenTimeoutFlow(),
                userPreferencesRepository.getDefaultScreenTimeoutFlow(),
            ) { currentScreenTimeout, keepOnIsActive, timeoutIconStyle, selectedTimeouts, defaultTimeout ->
                WidgetUIState.Success(
                    currentScreenTimeout = currentScreenTimeout,
                    keepOnIsActive = keepOnIsActive,
                    timeoutIconStyle = timeoutIconStyle,
                    selectedTimeouts = selectedTimeouts,
                    defaultTimeout = defaultTimeout
                )
            }.firstOrNull() ?: WidgetUIState.Error("Error updating widget UI state")
        }
    }

    private fun updateWidgetUIStateJob(context: Context): Job {
        val serviceScope = CoroutineScope(
            (context.applicationContext as KeepOnApplication)
                .applicationScope.coroutineContext + updateWidgetJob
        )

        return serviceScope.launch {
            withContext(Dispatchers.IO) {
                val widgetsCount = GlanceAppWidgetManager(context.applicationContext)
                    .getGlanceIds(KeepOnWidget::class.java)
                    .size

                if (widgetsCount <= 0) return@withContext

                mutex.withLock {
                    _currentWidgetUIState.update { getWidgetUIState(context) }
                }
            }
        }
    }

    suspend fun getWidgetPreviewUIState(context: Context): WidgetUIState {
        return getWidgetUIState(context)
    }

    suspend fun updateWidgetUIState(context: Context) {
        currentUpdateJob.cancelOrJoin()
        currentUpdateJob.job = updateWidgetUIStateJob(context)
    }
}
