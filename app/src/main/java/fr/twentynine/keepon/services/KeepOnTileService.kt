/*
package fr.twentynine.keepon.services

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
import androidx.lifecycle.lifecycleScope
import coil3.Image
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.lifecycle
import coil3.size.Size
import coil3.target.Target
import coil3.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.model.QSTimeoutData
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.BundleScrubber
import fr.twentynine.keepon.util.LockableJob
import fr.twentynine.keepon.util.RequiredPermissionsManager
import fr.twentynine.keepon.util.StringResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class KeepOnTileService : TileService(), LifecycleOwner {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var stringResourceProvider: StringResourceProvider

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    private lateinit var newQSTimeoutData: QSTimeoutData

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private val coroutineDispatcher = Dispatchers.IO

    private val iconSize = TimeoutIconSize.MEDIUM

    private var currentJob: LockableJob = LockableJob()

    private val qsCoilTarget: Target = object : Target {
        override fun onSuccess(result: Image) {
            if (qsTile == null) {
                return
            }
            lifecycleScope.launch(coroutineDispatcher) {
                // Get the new QSTile icon with coil
                val newQsTileBitmap = result.toBitmap()

                // Get the previous QSTile data
                val previousQsTileState = qsTile.state
                val previousQsTileLabel = qsTile.label

                // Get the new KeepOn state
                val keepOnIsActive = userPreferencesRepository.getKeepOnIsActive()

                // Set the new QSTile data
                qsTile.icon = newQsTileBitmap.toIcon()
                qsTile.state = if (keepOnIsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

                // Build the new QSTile label
                val newQsTileLabel = buildString {
                    append(getString(R.string.qs_service_name))
                    append(" - ")
                    append(newQSTimeoutData.iconData.iconTimeout.getFullDisplayTimeout(stringResourceProvider))
                }

                // Force update icon if only icon is changed
                if (previousQsTileState == qsTile.state && previousQsTileLabel == newQsTileLabel) {
                    qsTile.label = buildString {
                        append(newQsTileLabel)
                        append(" ")
                    }
                    qsTile.updateTile()
                }

                // Set the new label and update the QSTile
                qsTile.label = newQsTileLabel
                qsTile.updateTile()
            }
        }

        override fun onError(error: Image?) {
            qsTile.icon = Icon.createWithResource(this@KeepOnTileService, R.drawable.ic_keepon)
            qsTile.label = getString(R.string.qs_service_name)

            qsTile.updateTile()

            lifecycleScope.launch {
                delay(DELAY_BEFORE_RETRY_UPDATE)
                requestQSTileUpdate()
            }
        }
    }

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        lifecycleScope.launch(coroutineDispatcher) {
            userPreferencesRepository.setQSTileAdded(true)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        lifecycleScope.launch(coroutineDispatcher) {
            currentJob.cancelOrJoin()
            currentJob.job = launch(coroutineDispatcher) {
                updateQSTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()

        lifecycleScope.launch(coroutineDispatcher) {
            val currentScreenTimeout = userPreferencesRepository.getCurrentScreenTimeout()
            val filteredSelectedScreenTimeouts = userPreferencesRepository.getSelectedScreenTimeouts()
                .filter { screenTimeout -> screenTimeout != currentScreenTimeout }

            if (filteredSelectedScreenTimeouts.isEmpty() || !RequiredPermissionsManager.isPermissionsGranted(this@KeepOnTileService)) {
                startMainActivityAndCollapse()
            } else {
                if (!isLocked) {
                    userPreferencesRepository.setNextSelectedSystemScreenTimeout {
                        currentJob.cancelOrJoin()
                        currentJob.lock()
                        currentJob.job = launch(coroutineDispatcher) {
                            updateQSTile()
                        }
                    }
                }
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
            // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
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
    }

    private suspend fun updateQSTile() {
        withContext(coroutineDispatcher) {
            if (qsTile == null) {
                return@withContext
            }

            // Set the new QSTile data
            val newTimeoutIconData = TimeoutIconData(
                userPreferencesRepository.getCurrentScreenTimeout(),
                iconSize,
                userPreferencesRepository.getTimeoutIconStyle()
            )
            newQSTimeoutData = QSTimeoutData(
                keepOnState = userPreferencesRepository.getKeepOnIsActive(),
                iconData = newTimeoutIconData
            )

            // Apply the new QSTile data with coil
            val request = ImageRequest.Builder(this@KeepOnTileService)
                .data(newTimeoutIconData)
                .target(qsCoilTarget)
                .size(Size.ORIGINAL)
                .fetcherCoroutineContext(coroutineContext)
                .decoderCoroutineContext(coroutineContext)
                .lifecycle(lifecycle)
                .build()

            imageLoader.execute(request)
        }
    }

    private fun requestQSTileUpdate() {
        requestListeningState(
            this.applicationContext,
            ComponentName(this.applicationContext, KeepOnTileService::class.java)
        )
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startMainActivityAndCollapse() {
        val mainIntent = Intent(this@KeepOnTileService, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
        val mainPendingIntent = PendingIntent.getActivity(
            this@KeepOnTileService,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val startActivity = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(mainPendingIntent)
            } else {
                startActivityAndCollapse(mainIntent)
            }
        }

        if (isLocked) {
            unlockAndRun { startActivity() }
        } else {
            startActivity()
        }
    }

    companion object {
        private val DELAY_BEFORE_RETRY_UPDATE = TimeUnit.SECONDS.toMillis(50)
    }
}
*/
package fr.twentynine.keepon.services

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
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.lifecycle
import coil3.size.Size
import coil3.target.Target
import coil3.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.model.QSTimeoutData
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.BundleScrubber
import fr.twentynine.keepon.util.LockableJob
import fr.twentynine.keepon.util.RequiredPermissionsManager
import fr.twentynine.keepon.util.StringResourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class KeepOnTileService : TileService(), LifecycleOwner {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var stringResourceProvider: StringResourceProvider

    private val serviceJob = SupervisorJob()

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    private lateinit var newQSTimeoutData: QSTimeoutData

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private val coroutineDispatcher = Dispatchers.IO

    private val iconSize = TimeoutIconSize.MEDIUM

    private var currentJob: LockableJob = LockableJob()

    private val qsCoilTarget: Target = object : Target {
        override fun onSuccess(result: Image) {
            val tile = qsTile ?: return

            // Perform image processing and preference fetching in a background coroutine
            serviceScope.launch(coroutineDispatcher) {
                // Get the new QSTile icon with coil
                val newQsTileBitmap = result.toBitmap()

                // Get the previous QSTile data
                val previousQsTileState = tile.state
                val previousQsTileLabel = tile.label

                // Get the new KeepOn state
                val keepOnIsActive = userPreferencesRepository.getKeepOnIsActive()

                // Set the new QSTile data
                tile.icon = newQsTileBitmap.toIcon()
                tile.state = if (keepOnIsActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

                // Build the new QSTile label
                val timeoutDisplay = newQSTimeoutData.iconData.iconTimeout.getFullDisplayTimeout(
                    stringResourceProvider
                )
                val newQsTileLabel = "${getString(R.string.qs_service_name)} - $timeoutDisplay"

                // Force update icon if only icon is changed
                if (previousQsTileState == tile.state && previousQsTileLabel == newQsTileLabel) {
                    tile.label = "$newQsTileLabel "
                    withContext(Dispatchers.Main.immediate) {
                        tile.updateTile()
                    }
                }

                // Set the new label and update the QSTile
                tile.label = newQsTileLabel
                withContext(Dispatchers.Main.immediate) {
                    tile.updateTile()
                }
            }
        }

        override fun onError(error: Image?) {
            qsTile?.let { tile ->
                serviceScope.launch(Dispatchers.Main.immediate) {
                    tile.icon = Icon.createWithResource(this@KeepOnTileService, R.drawable.ic_keepon)
                    tile.label = getString(R.string.qs_service_name)
                    tile.state = Tile.STATE_UNAVAILABLE
                    tile.updateTile()
                }

                serviceScope.launch(coroutineDispatcher) {
                    delay(DELAY_BEFORE_RETRY_UPDATE)
                    requestQSTileUpdate()
                }
            }
        }
    }

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        serviceScope.launch(coroutineDispatcher) {
            userPreferencesRepository.setQSTileAdded(true)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        // Launch update task
        serviceScope.launch(coroutineDispatcher) {
            currentJob.cancelOrJoin()
            currentJob.job = launch(coroutineDispatcher) {
                updateQSTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()

        serviceScope.launch(coroutineDispatcher) {
            val selectedTimeouts = userPreferencesRepository.getSelectedScreenTimeouts()

            val filteredSelectedScreenTimeouts = selectedTimeouts
                .filter { screenTimeout -> screenTimeout != userPreferencesRepository.getCurrentScreenTimeout() }

            if (filteredSelectedScreenTimeouts.isEmpty() || !RequiredPermissionsManager.isPermissionsGranted(this@KeepOnTileService)) {
                withContext(Dispatchers.Main.immediate) {
                    startMainActivityAndCollapse()
                }
            } else {
                if (!isLocked) {
                    userPreferencesRepository.setNextSelectedSystemScreenTimeout {
                        currentJob.cancelOrJoin()
                        currentJob.lock()
                        currentJob.job = launch(coroutineDispatcher) {
                            updateQSTile()
                        }
                    }
                }
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
        serviceScope.launch(coroutineDispatcher) {
            currentJob.cancelOrJoin()
        }
    }

    private suspend fun updateQSTile() {
        val currentScreenTimeout = userPreferencesRepository.getCurrentScreenTimeout()
        val timeoutIconStyle = userPreferencesRepository.getTimeoutIconStyle()
        val keepOnState = userPreferencesRepository.getKeepOnIsActive()

        // Prepare data for the tile
        val newTimeoutIconData = TimeoutIconData(
            currentScreenTimeout,
            iconSize,
            timeoutIconStyle
        )

        newQSTimeoutData = QSTimeoutData(
            keepOnState = keepOnState,
            iconData = newTimeoutIconData
        )

        // Apply the new QSTile data with coil
        val request = ImageRequest.Builder(this@KeepOnTileService)
            .data(newTimeoutIconData)
            .target(qsCoilTarget)
            .size(Size.ORIGINAL)
            .lifecycle(lifecycle)
            .build()

        imageLoader.execute(request)
    }

    private fun requestQSTileUpdate() {
        requestListeningState(
            this.applicationContext,
            ComponentName(this.applicationContext, KeepOnTileService::class.java)
        )
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startMainActivityAndCollapse() {
        val mainIntent = Intent(this@KeepOnTileService, MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val mainPendingIntent = PendingIntent.getActivity(
            this@KeepOnTileService,
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
        private val DELAY_BEFORE_RETRY_UPDATE = TimeUnit.SECONDS.toMillis(10)
    }
}
