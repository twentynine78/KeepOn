package fr.twentynine.keepon.services

import android.content.ComponentName
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.utils.glide.TimeoutIconData
import fr.twentynine.keepon.ui.MainActivity
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class KeepOnTileService : TileService(), LifecycleOwner {

    private val commonUtils: CommonUtils by lazy()
    private val bundleScrubber: BundleScrubber by lazy()
    private val preferences: Preferences by lazy()
    private val glideApp: RequestManager by lazy()

    private val qsGlideTarget: CustomTarget<Bitmap> by lazy {
        object : CustomTarget<Bitmap>(50.px, 50.px) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (qsTile != null) {
                    qsTile.state =
                        if (preferences.getKeepOnState()) {
                            Tile.STATE_ACTIVE
                        } else {
                            Tile.STATE_INACTIVE
                        }

                    qsTile.icon = Icon.createWithBitmap(resource)

                    qsTile.updateTile()
                }
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        }
    }

    private val dispatcher = ServiceLifecycleDispatcher(this)

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private var lastSetTimeout: Int
        get() = globalLastSetTimeout
        set(value) { globalLastSetTimeout = value }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        if (intent != null) {
            // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
            if (bundleScrubber.scrub(intent) || (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)) {
                return null
            }
        }

        if (!preferences.getTileAdded()) {
            preferences.setTileAdded(true)
            requestListeningState(this, ComponentName(this, this::class.java))
        }

        commonUtils.startScreenTimeoutObserverService()

        return super.onBind(intent)
    }

    override fun onTileAdded() {
        preferences.setTileAdded(true)

        requestListeningState(this, ComponentName(this, this::class.java))

        commonUtils.startScreenTimeoutObserverService()

        super.onTileAdded()
    }

    override fun onTileRemoved() {
        preferences.setTileAdded(false)

        super.onTileRemoved()

        stopSelf()
    }

    override fun onStartListening() {
        super.onStartListening()

        if (qsTile != null) {
            if (preferences.getAppIsLaunched()) {
                val newTimeout = preferences.getCurrentTimeout()

                if (lastSetTimeout != newTimeout) {
                    // Create bitmap and load to tile icon
                    glideApp
                        .asBitmap()
                        .priority(Priority.HIGH)
                        .load(TimeoutIconData(newTimeout, 2, commonUtils.getIconStyleSignature()))
                        .into(qsGlideTarget)
                } else {
                    qsTile.updateTile()
                }
                lastSetTimeout = newTimeout
            } else {
                qsTile.state = Tile.STATE_UNAVAILABLE
                qsTile.updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()

        if (preferences.getSkipIntro()) {
            if (preferences.getSelectedTimeout().size < 1 || !Settings.System.canWrite(this)) {
                if (preferences.getKeepOnState()) {
                    preferences.setTimeout(preferences.getOriginalTimeout())
                } else {
                    val mainIntent = MainActivity.newIntent(this.applicationContext)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    if (preferences.getSelectedTimeout().size < 1) {
                        mainIntent.action = MainActivity.ACTION_MISSING_SETTINGS
                        commonUtils.sendBroadcastMissingSettings()
                    }
                    startActivityAndCollapse(mainIntent)
                }
            } else {
                preferences.setTimeout(preferences.getNextTimeoutValue())
            }
        }
    }

    override fun getLifecycle(): Lifecycle {
        return dispatcher.lifecycle
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    @Suppress("deprecation")
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    companion object {
        private var globalLastSetTimeout = -1
    }
}
