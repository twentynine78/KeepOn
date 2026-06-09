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
import fr.twentynine.keepon.ui.state.TaskerEditUIState
import fr.twentynine.keepon.ui.event.TaskerUIEvent
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.screen.TaskerEditRoute
import fr.twentynine.keepon.ui.viewmodel.TaskerEditViewModel
import fr.twentynine.keepon.core.tasker.PluginBundleManager
import fr.twentynine.keepon.core.tasker.TaskerIntent
import fr.twentynine.keepon.core.util.BundleScrubber

/**
 * Tasker/Locale plug-in "edit setting" activity: lets the user pick a timeout for a Tasker action and
 * returns it in the plug-in result bundle (with a display blurb). Validates the incoming intent
 * (action, package, bundle) and pre-selects the forwarded timeout when editing an existing action.
 */
@AndroidEntryPoint
class EditActivity : BasePermissionActivity() {

    private val taskerEditViewModel: TaskerEditViewModel by viewModels()

    private var isCancelled = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                taskerEditViewModel.uiState.value is TaskerEditUIState.Loading
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

        // Check that the Intent action will be ACTION_EDIT_SETTING
        if (TaskerIntent.ACTION_EDIT_SETTING != intent.action) {
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

        if (forwardedBundle != null && !PluginBundleManager.isBundleValid(forwardedBundle)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (forwardedBundle != null) {
            val forwardedTimeout = forwardedBundle
                .getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, -1)

            if (forwardedTimeout != -1 &&
                forwardedTimeout <= taskerEditViewModel.getMaxAllowedScreenTimeout()
            ) {
                taskerEditViewModel.setInitialSelectedScreenTimeout(forwardedTimeout)
            }
        }

        setContent {
            KeepOnTheme {
                val onEvent: (TaskerUIEvent) -> Unit = { event ->
                    when (event) {
                        TaskerUIEvent.RequestWriteSystemSettingPermission -> requestWriteSystemSettingPermission()
                        TaskerUIEvent.RequestDisableBatteryOptimization -> requestDisableBatteryOptimization()
                        TaskerUIEvent.RequestPostNotification -> requestPostNotificationPermission()
                        TaskerUIEvent.CheckNeededPermissions -> checkNeededPermissions()
                        else -> taskerEditViewModel.onEvent(event)
                    }
                }

                val uiState by taskerEditViewModel.uiState.collectAsStateWithLifecycle()

                TaskerEditRoute(
                    uiState = uiState,
                    onEvent = onEvent,
                    saveTaskerConfiguration = {
                        isCancelled = (taskerEditViewModel.uiState.value as? TaskerEditUIState.Success)?.selectedScreenTimeout == null
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
            val selectedTimeout = (taskerEditViewModel.uiState.value as? TaskerEditUIState.Success)?.selectedScreenTimeout

            if (selectedTimeout == null) {
                setResult(RESULT_CANCELED)
            } else {
                val timeout = selectedTimeout.value
                val timeoutText = selectedTimeout.displayName

                val resultBundle = Bundle()
                resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, timeout)

                val resultIntent = Intent()
                resultIntent.putExtra(TaskerIntent.EXTRA_BUNDLE, resultBundle)

                // add text for display in tasker
                resultIntent.putExtra(TaskerIntent.EXTRA_STRING_BLURB, timeoutText)

                setResult(RESULT_OK, resultIntent)
            }
        }

        super.finish()
    }
}
