package fr.twentynine.keepon.tasker

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.data.model.TaskerEditUIState
import fr.twentynine.keepon.data.model.TaskerUIEvent
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.view.TaskerEditView
import fr.twentynine.keepon.ui.viewmodel.TaskerEditViewModel
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.BundleScrubber
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditActivity : ComponentActivity() {

    private val taskerEditViewModel: TaskerEditViewModel by viewModels()

    @Inject
    lateinit var systemSettingPermissionManager: SystemSettingPermissionManager

    @Inject
    lateinit var postNotificationPermissionManager: PostNotificationPermissionManager

    @Inject
    lateinit var batteryOptimizationManager: BatteryOptimizationManager

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    private var uiState = mutableStateOf<TaskerEditUIState>(TaskerEditUIState.Loading)

    private var isCancelled = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                uiState.value is TaskerEditUIState.Loading
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

        taskerEditViewModel.initViewModel(
            systemSettingPermissionManager,
            postNotificationPermissionManager,
            batteryOptimizationManager,
        )

        lifecycleScope.launch(Dispatchers.IO) {
            taskerEditViewModel.getUiState()
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { newUIState ->
                    uiState.value = newUIState
                }
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

        val onEvent: (TaskerUIEvent) -> Unit = { event -> taskerEditViewModel.onEvent(event) }

        setContent {
            KeepOnTheme {
                val requestPostNotificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    taskerEditViewModel.updatePostNotificationPermission(isGranted)
                    if (isGranted) {
                        lifecycleScope.launch {
                            screenOffReceiverServiceManager.restartService()
                        }
                    }
                }

                LaunchedEffect(taskerEditViewModel) {
                    taskerEditViewModel.setManagedActivityResultLauncher(
                        requestPostNotificationPermissionLauncher
                    )
                }

                TaskerEditView(
                    uiState = uiState.value,
                    onEvent = onEvent,
                    saveTaskerConfiguration = {
                        isCancelled = (uiState.value as TaskerEditUIState.Success).selectedScreenTimeout == null
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
            val selectedTimeout = (uiState.value as TaskerEditUIState.Success).selectedScreenTimeout

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

    override fun onResume() {
        super.onResume()

        checkWriteSystemSettingsPermission()
        checkBatteryOptimizationState()
        checkPostNotificationPermission()
    }

    private fun checkWriteSystemSettingsPermission() {
        taskerEditViewModel.checkWriteSystemSettingsPermission()
    }

    private fun checkBatteryOptimizationState() {
        taskerEditViewModel.checkBatteryOptimizationState()
    }

    private fun checkPostNotificationPermission() {
        taskerEditViewModel.checkPostNotificationPermission()
    }
}
