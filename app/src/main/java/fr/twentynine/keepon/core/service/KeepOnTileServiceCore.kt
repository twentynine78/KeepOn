package fr.twentynine.keepon.core.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.graphics.drawable.toIcon
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.lifecycle
import coil3.size.Size
import coil3.toBitmap
import fr.twentynine.keepon.di.qualifier.ApplicationScope
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import fr.twentynine.keepon.domain.usecase.preferences.SetQSTileAddedUseCase
import fr.twentynine.keepon.domain.usecase.timeout.SetNextSystemScreenTimeoutUseCase
import fr.twentynine.keepon.domain.usecase.timeout.ShouldRouteToAppUseCase
import fr.twentynine.keepon.core.transition.TransitionPlayer
import fr.twentynine.keepon.core.util.BundleScrubber
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
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

/**
 * Shared implementation of the quick-settings tile, made `open` so the manifest-declared
 * [fr.twentynine.keepon.services.KeepOnTileService] can subclass it. It owns its own lifecycle (to
 * drive Coil), reactively reflects the current timeout and active state on the tile, cycles to the
 * next timeout (or routes to the app) on click, and plays the icon-change transition between tile
 * icons. The icon bitmaps are produced by the same Coil pipeline as the rest of the app.
 */
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
    lateinit var shouldRouteToAppUseCase: ShouldRouteToAppUseCase

    @Inject
    lateinit var setQSTileAddedUseCase: SetQSTileAddedUseCase

    @Inject
    lateinit var stringResourceProvider: StringResourceProvider

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    private val serviceJob = SupervisorJob()
    private val serviceScope by lazy { CoroutineScope(applicationScope.coroutineContext + serviceJob) }

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private val iconSize = TimeoutIconSize.MEDIUM

    // Active while the tile is listening (QS panel open): observes the state and
    // redraws on every change, so the tile stays correct as long as it is visible.
    private var listeningJob: Job? = null

    // Last crisp icon + timeout drawn: lets a redraw animate from the previous icon and tell a
    // real timeout change apart from the first render / QS-panel reopen / a pure style change.
    // Volatile: cleared from the main thread in onTrimMemory, read/written on the Default collector.
    @Volatile
    private var lastIconBitmap: Bitmap? = null
    private var lastShownTimeout: ScreenTimeout? = null

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
                uiPreferencesRepository.getIconTransitionAnimationFlow(),
            ) { currentTimeout, iconStyle, keepOnIsActive, transition ->
                TileRenderState(currentTimeout, iconStyle, keepOnIsActive, transition)
            }
                .distinctUntilChanged()
                .collect { state -> updateQSTile(state) }
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
            if (shouldRouteToAppUseCase()) {
                withContext(Dispatchers.Main.immediate) {
                    startMainActivityAndCollapse()
                }
            } else {
                // The listening collector redraws (and animates) the tile once the new
                // timeout is persisted.
                setNextSystemScreenTimeoutUseCase()
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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Drop the cached previous icon under memory pressure; the next redraw re-fetches it from
        // Coil (a real timeout change then renders without the from→to animation, which is fine).
        lastIconBitmap = null
    }

    private suspend fun updateQSTile(state: TileRenderState) {
        val newTimeoutIconData = TimeoutIconData(
            state.currentScreenTimeout,
            iconSize,
            state.timeoutIconStyle
        )

        val request = imageRequestBuilder
            .data(newTimeoutIconData)
            .build()

        when (val result = imageLoader.execute(request)) {
            is SuccessResult -> {
                qsTile?.let { tile ->
                    val timeoutDisplay =
                        newTimeoutIconData.iconTimeout.getFullDisplayTimeout(stringResourceProvider)

                    tile.state = if (state.keepOnIsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                    tile.label = "${getString(R.string.qs_service_name)} - $timeoutDisplay"

                    val newBitmap = result.image.toBitmap()
                    val previousBitmap = lastIconBitmap
                    // Animate only a real timeout change (a previous icon exists and the value
                    // actually changed): excludes the first render, QS-panel reopen and pure
                    // style changes, which update instantly.
                    val timeoutChanged = lastShownTimeout != null &&
                        state.currentScreenTimeout != lastShownTimeout
                    if (state.transition.enabled && previousBitmap != null && timeoutChanged) {
                        val transition = IconTransitionCatalog.fromId(state.transition.typeId)
                        val durationMs = IconTransitionTiming.durationMs(state.transition.durationStep)
                        playTransition(tile, previousBitmap, newBitmap, transition, durationMs)
                    } else {
                        tile.icon = newBitmap.toIcon()
                        tile.updateTile()
                    }
                    lastIconBitmap = newBitmap
                    lastShownTimeout = state.currentScreenTimeout
                }
            }

            is ErrorResult -> {
                qsTile?.let { tile ->
                    tile.icon = Icon.createWithResource(this, R.drawable.ic_keepon)
                    tile.label = getString(R.string.qs_service_name)
                    tile.state = Tile.STATE_UNAVAILABLE
                    tile.updateTile()

                    // Retry off the collector so a slow retry does not block redraws.
                    serviceScope.launch {
                        delay(DELAY_BEFORE_RETRY_UPDATE)
                        requestQSTileUpdate()
                    }
                }
            }
        }
    }

    /**
     * Plays the configured transition by pushing successive composite frames through
     * [Tile.updateTile] (ease-out over [IconTransitionTiming.FRAME_COUNT] frames), then settles
     * on the crisp target icon.
     */
    private suspend fun playTransition(
        tile: Tile,
        oldBitmap: Bitmap,
        newBitmap: Bitmap,
        transition: IconTransition,
        durationMs: Int,
    ) {
        TransitionPlayer.play(
            transition = transition,
            from = oldBitmap,
            to = newBitmap,
            durationMs = durationMs,
            maxFrames = IconTransitionTiming.FRAME_COUNT,
        ) { frame ->
            tile.icon = frame.toIcon()
            tile.updateTile()
        }
        tile.icon = newBitmap.toIcon()
        tile.updateTile()
    }

    private data class TileRenderState(
        val currentScreenTimeout: ScreenTimeout,
        val timeoutIconStyle: TimeoutIconStyle,
        val keepOnIsActive: Boolean,
        val transition: IconTransitionAnimation,
    )

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
