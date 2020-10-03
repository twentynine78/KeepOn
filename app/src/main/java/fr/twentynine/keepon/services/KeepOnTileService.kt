package fr.twentynine.keepon.services

import android.app.Service
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
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.glide.GlideApp
import fr.twentynine.keepon.glide.TimeoutIconData
import fr.twentynine.keepon.utils.KeepOnUtils


class KeepOnTileService: TileService() {
    private lateinit var glideRequestBuilder: RequestBuilder<Bitmap>
    private lateinit var glideRequestManager: RequestManager
    private lateinit var glideTarget: CustomTarget<Bitmap>
    private lateinit var glide: Glide

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()

        // Set glide components
        glide = GlideApp.get(this)
        glideRequestManager = GlideApp.with(this)
        setGlideRequestBuilder()
        setGlideTarget()
    }

    override fun onDestroy() {
        // Clear glide target and clear memory
        glideRequestManager.clear(glideTarget)

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        KeepOnUtils.setTileAdded(true, this)

        KeepOnUtils.startScreenTimeoutObserverService(this)

        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
        return super.onBind(intent)
    }

    override fun onTileAdded() {
        super.onTileAdded()

        KeepOnUtils.setTileAdded(true, this)

        if (!KeepOnUtils.getKeepOnState(this))
            KeepOnUtils.updateOriginalTimeout(this)

        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
    }

    override fun onTileRemoved() {
        KeepOnUtils.setTimeout(KeepOnUtils.getOriginalTimeout(this), this)

        KeepOnUtils.stopScreenTimeoutObserverService(this)
        KeepOnUtils.stopScreenOffReceiverService(this)

        KeepOnUtils.setTileAdded(false, this)

        super.onTileRemoved()

        stopSelf()
    }

    override fun onStartListening() {
        super.onStartListening()

        if (qsTile.state == Tile.STATE_UNAVAILABLE) this.stopSelf()

        val newTimeout = KeepOnUtils.getCurrentTimeout(this)

        // Clear previous glide target
        glideRequestManager.clear(glideTarget)

        // Create bitmap and load to tile icon
        glideRequestBuilder
            .load(TimeoutIconData(newTimeout, 2, KeepOnUtils.getIconStyleSignature(this)))
            .into(glideTarget)
    }

    override fun onClick() {
        super.onClick()
        KeepOnUtils.setTileAdded(true, this)

        if (KeepOnUtils.getSkipIntro(this)) {
            if (KeepOnUtils.getOriginalTimeout(this) == 0
                || KeepOnUtils.getSelectedTimeout(this).size < 1
                || !Settings.System.canWrite(this)
            ) {
                val mainIntent = MainActivity.newIntent(this.applicationContext)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (KeepOnUtils.getSelectedTimeout(this).size < 1 && Settings.System.canWrite(this)) {
                    mainIntent.putExtra(KeepOnUtils.TAG_MISSING_SETTINGS, true)
                    KeepOnUtils.sendBroadcastMissingSettings(this)
                }

                startActivityAndCollapse(mainIntent)
                return
            }

            KeepOnUtils.startScreenTimeoutObserverService(this)

            KeepOnUtils.setTimeout(KeepOnUtils.getNextTimeoutValue(this), this)
        }
    }

    private fun setGlideTarget() {
        glideTarget = object: CustomTarget<Bitmap>(50.px, 50.px) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val keeponTile = qsTile
                val tileIcon = Icon.createWithBitmap(resource)
                if (keeponTile != null && tileIcon != null) {
                    keeponTile.state = if (KeepOnUtils.getKeepOnState(this@KeepOnTileService)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                    keeponTile.icon = tileIcon

                    keeponTile.updateTile()
                }
            }
            override fun onLoadCleared(placeholder: Drawable?) {
            }
        }
    }

    private fun setGlideRequestBuilder() {
        glideRequestBuilder = glideRequestManager
            .asBitmap()
            .priority(Priority.HIGH)
    }
}