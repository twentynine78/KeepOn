package fr.twentynine.keepon.core.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.graphics.drawable.toIcon
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import coil3.Image
import coil3.executeBlocking
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.lifecycle
import coil3.size.Size
import coil3.target.Target
import coil3.toBitmap
import fr.twentynine.keepon.KeepOnApplication
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.domain.usecase.preferences.SetQSTileAddedUseCase
import fr.twentynine.keepon.domain.usecase.timeout.SetNextSystemScreenTimeoutUseCase
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.gateway.WidgetUpdater
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class KeepOnTileServiceCore : TileService(), LifecycleOwner {

    @Inject
    lateinit var timeoutPreferencesRepository: TimeoutPreferencesRepository

    @Inject
    lateinit var uiPreferencesRepository: UiPreferencesRepository

    @Inject
    lateinit var getKeepOnStatusUseCase: GetKeepOnStatusUseCase

    @Inject
    lateinit var setNextSystemScreenTimeoutUseCase: SetNextSystemScreenTimeoutUseCase

    @Inject
    lateinit var setQSTileAddedUseCase: SetQSTileAddedUseCase

    @Inject
    lateinit var stringResourceProvider: StringResourceProvider

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    @Inject
    lateinit var permissionStateGateway: PermissionStateGateway

    private val serviceJob = SupervisorJob()
    private val applicationScope by lazy { (this.applicationContext as KeepOnApplication).applicationScope }
    private val serviceScope by lazy { CoroutineScope(applicationScope.coroutineContext + serviceJob) }

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private val iconSize = TimeoutIconSize.MEDIUM

    // Active while the tile is listening (QS panel open): observes the state and
    // redraws on every change, so the tile stays correct as long as it is visible.
    private var listeningJob: Job? = null

    private val imageRequestBuilder by lazy {
        ImageRequest.Builder(this)
            .size(Size.ORIGINAL)
            .lifecycle(lifecycle)
    }

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        serviceScope.launch {
            setQSTileAddedUseCase(true)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        listeningJob?.cancel()
        listeningJob = serviceScope.launch {
            combine(
                timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
                uiPreferencesRepository.getTimeoutIconStyleFlow(),
                getKeepOnStatusUseCase(),
            ) { currentTimeout, iconStyle, keepOnIsActive ->
                Triple(currentTimeout, iconStyle, keepOnIsActive)
            }
                .distinctUntilChanged()
                .collect { (currentTimeout, iconStyle, keepOnIsActive) ->
                    updateQSTile(currentTimeout, iconStyle, keepOnIsActive)
                }
        }
    }

    override fun onStopListening() {
        super.onStopListening()

        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onClick() {
        super.onClick()

        if (isLocked) {
            return
        }

        serviceScope.launch {
            val selectedTimeouts = timeoutPreferencesRepository.getSelectedScreenTimeouts()
            val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
            val currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()

            val timeoutsWithDefault = if (selectedTimeouts.contains(defaultTimeout)) {
                selectedTimeouts
            } else {
                listOf(defaultTimeout) + selectedTimeouts
            }
            val filteredSelectedScreenTimeouts = timeoutsWithDefault
                .filter { screenTimeout -> screenTimeout != currentTimeout }

            if (filteredSelectedScreenTimeouts.isEmpty() || !permissionStateGateway.areRequiredPermissionsGranted()) {
                withContext(Dispatchers.Main.immediate) {
                    startMainActivityAndCollapse()
                }
            } else {
                // The listening collector redraws the tile once the new timeout is persisted.
                setNextSystemScreenTimeoutUseCase(currentTimeout)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        lifecycleDispatcher.onServicePreSuperOnBind()

        if (intent != null) {
            if (BundleScrubber.scrub(intent) ||
                (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)
            ) {
                return null
            }
        }
        return super.onBind(intent)
    }

    override fun onTileAdded() {
        super.onTileAdded()

        requestQSTileUpdate()
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()

        serviceJob.cancel()
    }

    private fun updateQSTile(
        currentScreenTimeout: ScreenTimeout,
        timeoutIconStyle: TimeoutIconStyle,
        keepOnIsActive: Boolean,
    ) {
        val newTimeoutIconData = TimeoutIconData(
            currentScreenTimeout,
            iconSize,
            timeoutIconStyle
        )

        val qsCoilTarget = object : Target {
            override fun onSuccess(result: Image) {
                val tile = qsTile ?: return

                val newQsTileBitmap = result.toBitmap()

                tile.icon = newQsTileBitmap.toIcon()
                tile.state = if (keepOnIsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

                val timeoutDisplay =
                    newTimeoutIconData.iconTimeout.getFullDisplayTimeout(stringResourceProvider)
                tile.label = "${getString(R.string.qs_service_name)} - $timeoutDisplay"

                tile.updateTile()
            }

            override fun onError(error: Image?) {
                qsTile?.let { tile ->
                    tile.icon = Icon.createWithResource(this@KeepOnTileServiceCore, R.drawable.ic_keepon)
                    tile.label = getString(R.string.qs_service_name)
                    tile.state = Tile.STATE_UNAVAILABLE
                    tile.updateTile()

                    serviceScope.launch {
                        delay(DELAY_BEFORE_RETRY_UPDATE)
                        requestQSTileUpdate()
                    }
                }
            }
        }

        // Apply the new QSTile data with coil
        val request = imageRequestBuilder
            .data(newTimeoutIconData)
            .target(qsCoilTarget)
            .build()

        imageLoader.executeBlocking(request)

        // Request widget update
        serviceScope.launch {
            widgetUpdater.requestUpdateWidget()
        }
    }

    private fun requestQSTileUpdate() {
        requestListeningState(
            this.applicationContext,
            ComponentName(this.applicationContext, javaClass)
        )
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startMainActivityAndCollapse() {
        val mainIntent = Intent(this@KeepOnTileServiceCore, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val mainPendingIntent = PendingIntent.getActivity(
            this@KeepOnTileServiceCore,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val startActivityAction = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(mainPendingIntent)
            } else {
                startActivityAndCollapse(mainIntent)
            }
        }

        if (isLocked) {
            unlockAndRun { startActivityAction() }
        } else {
            startActivityAction()
        }
    }

    companion object {
        private val DELAY_BEFORE_RETRY_UPDATE = 10.seconds
    }
}
