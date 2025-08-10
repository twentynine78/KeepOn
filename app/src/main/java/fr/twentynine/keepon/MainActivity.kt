package fr.twentynine.keepon

import android.content.ComponentName
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
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.view.MainView
import fr.twentynine.keepon.ui.viewmodel.MainViewModel
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.BundleScrubber
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var systemSettingPermissionManager: SystemSettingPermissionManager

    @Inject
    lateinit var postNotificationPermissionManager: PostNotificationPermissionManager

    @Inject
    lateinit var batteryOptimizationManager: BatteryOptimizationManager

    @Inject
    lateinit var screenOffReceiverServiceManager: ScreenOffReceiverServiceManager

    private var uiState = mutableStateOf<MainViewUIState>(MainViewUIState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                uiState.value is MainViewUIState.Loading
            }
        }

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
        if (BundleScrubber.scrub(intent) ||
            (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)
        ) {
            finish()
            return
        }

        mainViewModel.initViewModel(
            systemSettingPermissionManager,
            postNotificationPermissionManager,
            batteryOptimizationManager,
        )

        lifecycleScope.launch(Dispatchers.IO) {
            mainViewModel.getUiState()
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest { newUIState ->
                    uiState.value = newUIState
                }

            mainViewModel.incrementAppLaunchCount()
        }

        val onEvent: (MainUIEvent) -> Unit = { event -> mainViewModel.onEvent(event) }

        setContent {
            KeepOnTheme {
                val requestPostNotificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    mainViewModel.updatePostNotificationPermission(isGranted)
                    if (isGranted) {
                        lifecycleScope.launch {
                            screenOffReceiverServiceManager.restartService()
                        }
                    }
                }

                LaunchedEffect(mainViewModel) {
                    mainViewModel.setManagedActivityResultLauncher(
                        requestPostNotificationPermissionLauncher
                    )
                }

                MainView(
                    uiState = uiState.value,
                    onEvent = onEvent
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        checkWriteSystemSettingsPermission()
        checkBatteryOptimizationState()
        checkPostNotificationPermission()

        startScreenOffReceiverServiceIfNeeded()
    }

    private fun checkWriteSystemSettingsPermission() {
        mainViewModel.checkWriteSystemSettingsPermission()
    }

    private fun checkBatteryOptimizationState() {
        mainViewModel.checkBatteryOptimizationState()
    }

    private fun checkPostNotificationPermission() {
        mainViewModel.checkPostNotificationPermission()
    }

    private fun startScreenOffReceiverServiceIfNeeded() {
        lifecycleScope.launch(Dispatchers.Default) {
            while (uiState.value !is MainViewUIState.Success) {
                delay(200)
            }

            val successUIState = uiState.value as MainViewUIState.Success

            if (successUIState.keepOnIsActive && successUIState.resetTimeoutWhenScreenOff) {
                screenOffReceiverServiceManager.startService()
            }
        }
    }
}
