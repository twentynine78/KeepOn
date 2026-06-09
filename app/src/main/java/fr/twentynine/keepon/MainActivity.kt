package fr.twentynine.keepon

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.screen.MainScreen
import fr.twentynine.keepon.ui.viewmodel.MainViewModel
import fr.twentynine.keepon.core.util.BundleScrubber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * The app's single Compose activity. Holds the splash screen until the [MainViewModel] state leaves
 * Loading, hosts [MainScreen], and routes permission-request events to the
 * [BasePermissionActivity] wiring. On resume it starts the screen-off service when KeepOn is active
 * and configured to reset on screen-off.
 */
@AndroidEntryPoint
class MainActivity : BasePermissionActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                mainViewModel.uiState.value is MainViewUIState.Loading
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

        mainViewModel.incrementAppLaunchCount()

        setContent {
            KeepOnTheme {
                val onEvent: (MainUIEvent) -> Unit = remember {
                    { event ->
                        when (event) {
                            MainUIEvent.RequestWriteSystemSettingPermission -> requestWriteSystemSettingPermission()
                            MainUIEvent.RequestDisableBatteryOptimization -> requestDisableBatteryOptimization()
                            MainUIEvent.RequestPostNotification -> requestPostNotificationPermission()
                            MainUIEvent.CheckNeededPermissions -> checkNeededPermissions()
                            else -> mainViewModel.onEvent(event)
                        }
                    }
                }

                val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

                MainScreen(
                    uiState = uiState,
                    onEvent = onEvent
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        startScreenOffReceiverServiceIfNeeded()
    }

    private fun startScreenOffReceiverServiceIfNeeded() {
        lifecycleScope.launch(Dispatchers.Default) {
            val successUIState = mainViewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .filterIsInstance<MainViewUIState.Success>()
                .firstOrNull()

            if (successUIState != null && successUIState.keepOnIsActive && successUIState.resetTimeoutWhenScreenOff) {
                screenOffReceiverServiceManager.startService()
            }
        }
    }
}
