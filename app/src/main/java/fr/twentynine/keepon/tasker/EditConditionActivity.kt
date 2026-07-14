package fr.twentynine.keepon.tasker

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.BasePermissionActivity
import fr.twentynine.keepon.core.tasker.EventPluginBundleManager
import fr.twentynine.keepon.core.tasker.TaskerIntent
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.ui.event.TaskerEventUIEvent
import fr.twentynine.keepon.ui.screen.TaskerEventEditRoute
import fr.twentynine.keepon.ui.state.TaskerEventEditUIState
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.viewmodel.TaskerEventEditViewModel

/**
 * Tasker/Locale plug-in "edit condition" activity: lets the user pick the KeepOn event that will
 * trigger the automation condition and returns it in the plug-in result bundle (with a display
 * blurb). Validates the incoming intent (action, package, bundle) and pre-selects the forwarded
 * event when editing an existing condition.
 */
@AndroidEntryPoint
class EditConditionActivity : BasePermissionActivity() {

    private val taskerEventEditViewModel: TaskerEventEditViewModel by viewModels()

    private var isCancelled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                taskerEventEditViewModel.uiState.value is TaskerEventEditUIState.Loading
            }
        }

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Check that the Intent action will be ACTION_EDIT_CONDITION (pure-Locale hosts) or
        // ACTION_EDIT_EVENT (Tasker's Event category); both edit the same condition bundle.
        if (TaskerIntent.ACTION_EDIT_CONDITION != intent.action &&
            TaskerIntent.ACTION_EDIT_EVENT != intent.action
        ) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() &&
            ComponentName(this, this.javaClass.name) != intent.component
        ) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val forwardedBundle = intent.getBundleExtra(TaskerIntent.EXTRA_BUNDLE)

        // Nested bundles unparcel lazily, so the forwarded bundle needs its own scrub before
        // isBundleValid/getInt touch it.
        if (BundleScrubber.scrub(forwardedBundle)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (forwardedBundle != null) {
            if (!EventPluginBundleManager.isBundleValid(forwardedBundle)) {
                setResult(RESULT_CANCELED)
                finish()
                return
            }

            // The view model only preselects known event ids, so the missing-extra -1 simply
            // matches nothing.
            taskerEventEditViewModel.setInitialSelectedEvent(
                forwardedBundle.getInt(EventPluginBundleManager.BUNDLE_EXTRA_EVENT_TYPE, -1)
            )
        }

        setContent {
            KeepOnTheme {
                val onEvent: (TaskerEventUIEvent) -> Unit = { event ->
                    when (event) {
                        TaskerEventUIEvent.RequestWriteSystemSettingPermission -> requestWriteSystemSettingPermission()
                        TaskerEventUIEvent.RequestDisableBatteryOptimization -> requestDisableBatteryOptimization()
                        TaskerEventUIEvent.RequestPostNotification -> requestPostNotificationPermission()
                        TaskerEventUIEvent.CheckNeededPermissions -> checkNeededPermissions()
                        else -> taskerEventEditViewModel.onEvent(event)
                    }
                }

                val uiState by taskerEventEditViewModel.uiState.collectAsStateWithLifecycle()

                TaskerEventEditRoute(
                    uiState = uiState,
                    onEvent = onEvent,
                    saveTaskerConfiguration = {
                        // finish() still returns RESULT_CANCELED when no event is selected.
                        isCancelled = false
                        finish()
                    }
                )
            }
        }
    }

    override fun finish() {
        if (isCancelled) {
            setResult(RESULT_CANCELED)
        } else {
            val selectedEvent = (taskerEventEditViewModel.uiState.value as? TaskerEventEditUIState.Success)?.selectedEvent

            if (selectedEvent == null) {
                setResult(RESULT_CANCELED)
            } else {
                val resultBundle = Bundle()
                resultBundle.putInt(EventPluginBundleManager.BUNDLE_EXTRA_EVENT_TYPE, selectedEvent.type.id)

                val resultIntent = Intent()
                resultIntent.putExtra(TaskerIntent.EXTRA_BUNDLE, resultBundle)

                // add text for display in tasker
                resultIntent.putExtra(TaskerIntent.EXTRA_STRING_BLURB, selectedEvent.displayName)

                setResult(RESULT_OK, resultIntent)
            }
        }

        super.finish()
    }
}
