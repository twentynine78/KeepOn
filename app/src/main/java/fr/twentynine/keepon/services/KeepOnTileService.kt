package fr.twentynine.keepon.services

import android.app.Service
import android.content.ComponentName
import android.content.Intent
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
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.utils.KeepOnUtils


class KeepOnTileService: TileService() {
    private lateinit var glideRequestBuilder: RequestBuilder<Bitmap>
    private lateinit var glideRequestManager: RequestManager
    private lateinit var glideTarget: CustomTarget<Bitmap>
    private lateinit var glide: Glide

    override fun onCreate() {
        super.onCreate()

        // Set glide components
        glide = Glide.get(this)
        glideRequestManager = Glide.with(this)
        setGlideRequestBuilder()
        setGlideTarget()
    }

    override fun onDestroy() {
        // Clear glide target and clear memory
        glideRequestManager.clear(glideTarget)
        glide.clearMemory()

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

        if (!KeepOnUtils.getKeepOn(this))
            KeepOnUtils.updateOriginalTimeout(this)

        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
    }

    override fun onTileRemoved() {
        KeepOnUtils.setTimeout(KeepOnUtils.getOriginalTimeout(this), this)
        KeepOnUtils.setKeepOn(false, this)

        KeepOnUtils.stopScreenTimeoutObserverService(this)
        KeepOnUtils.stopScreenOffReceiverService(this)

        KeepOnUtils.setTileAdded(false, this)

        super.onTileRemoved()

        stopSelf()
    }

    override fun onStartListening() {
        super.onStartListening()

        if (qsTile.state == Tile.STATE_UNAVAILABLE) this.stopSelf()

        originalTimeout = KeepOnUtils.getOriginalTimeout(this)
        newTimeout = if (KeepOnUtils.getNewTimeout(this) >= 0) {
            KeepOnUtils.getNewTimeout(this)
        } else {
            KeepOnUtils.getCurrentTimeout(this)
        }
        KeepOnUtils.setNewTimeout(-1, this)

        // Clear previous glide target
        glideRequestManager.clear(glideTarget)

        // Create bitmap and load to tile icon
        glideRequestBuilder
            .signature(ObjectKey(KeepOnUtils.getBitmapSignature(this, newTimeout)))
            .load(KeepOnUtils.getBitmapFromText(newTimeout, this))
            .into(glideTarget)
    }

    override fun onClick() {
        super.onClick()
        KeepOnUtils.setTileAdded(true, this)

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

        val newTimeout = KeepOnUtils.getNextTimeoutValue(this)

        KeepOnUtils.setNewTimeout(newTimeout, this)
        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))

        // Apply new timeout
        if (newTimeout == KeepOnUtils.getOriginalTimeout(this)) {
            KeepOnUtils.setKeepOn(false, this)
            KeepOnUtils.stopScreenOffReceiverService(this)
        } else {
            KeepOnUtils.setKeepOn(true, this)
            if (KeepOnUtils.getResetOnScreenOff(this))
                KeepOnUtils.startScreenOffReceiverService(this)
        }

        KeepOnUtils.setTimeout(newTimeout, this)
    }

    private fun setGlideTarget() {
        glideTarget = object: CustomTarget<Bitmap>() {
            private var bitmap: Bitmap? = null
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmap = resource
                val keeponTile = qsTile
                val tileIcon = Icon.createWithBitmap(resource)
                if (keeponTile != null && tileIcon != null) {
                    keeponTile.state = if (newTimeout == originalTimeout) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
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
            .format(DecodeFormat.PREFER_ARGB_8888)
            .circleCrop()
            .priority(Priority.HIGH)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    }

    companion object {
        private var newTimeout: Int = 0
        private var originalTimeout: Int = 0
    }
}