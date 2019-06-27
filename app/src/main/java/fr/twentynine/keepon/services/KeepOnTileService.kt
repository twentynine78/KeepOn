package fr.twentynine.keepon.services

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.LruCache
import fr.twentynine.keepon.R
import fr.twentynine.keepon.SplashScreen
import fr.twentynine.keepon.utils.KeepOnUtils


class KeepOnTileService : TileService() {

    private lateinit var memoryCache: LruCache<String, Bitmap>

    override fun onCreate() {
        super.onCreate()
        memoryCache = object : LruCache<String, Bitmap>(2 * 1024 * 1024) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))

        if (!KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, this))
            KeepOnUtils.startScreenTimeoutObserverService(this)

        if (!KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, this)
            && KeepOnUtils.getKeepOn(this)
        ) {
            KeepOnUtils.startScreenOffReceiverService(this)
        }

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

        if (KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, this))
            KeepOnUtils.stopScreenTimeoutObserverService(this)

        if (KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, this))
            KeepOnUtils.stopScreenOffReceiverService(this)

        KeepOnUtils.setTileAdded(false, this)

        super.onTileRemoved()

        stopSelf()
    }

    override fun onStartListening() {
        super.onStartListening()

        val currentTimeout = KeepOnUtils.getCurrentTimeout(this)

        val keeponTile = qsTile

        if (keeponTile != null) {
            if (currentTimeout == KeepOnUtils.getOriginalTimeout(this))
                keeponTile.state = Tile.STATE_INACTIVE
            else
                keeponTile.state = Tile.STATE_ACTIVE

            keeponTile.icon = Icon.createWithBitmap(KeepOnUtils.getBitmapFromText(currentTimeout, memoryCache, this))
            keeponTile.label = getString(R.string.qs_tile_label)
            keeponTile.contentDescription = getString(R.string.qs_tile_desc)

            keeponTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        if (KeepOnUtils.getOriginalTimeout(this) == 0
            || KeepOnUtils.getSelectedTimeout(this).size <= 1
            || !Settings.System.canWrite(this)
        ) {
            val mainIntent = SplashScreen.newIntent(this.applicationContext)
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (KeepOnUtils.getSelectedTimeout(this).size <= 1)
                mainIntent.putExtra(KeepOnUtils.TAG_MISSING_SETTINGS, true)
            startActivityAndCollapse(mainIntent)

            KeepOnUtils.sendBroadcastMissingSettings(this)
            return
        }

        if (!KeepOnUtils.isMyServiceRunning(ScreenTimeoutObserverService::class.java, this))
            KeepOnUtils.startScreenTimeoutObserverService(this)

        val availableTimeout: ArrayList<Int> = ArrayList()
        availableTimeout.addAll(KeepOnUtils.getSelectedTimeout(this))
        availableTimeout.remove(KeepOnUtils.getOriginalTimeout(this))
        availableTimeout.add(KeepOnUtils.getOriginalTimeout(this))
        availableTimeout.sort()

        val currentTimeout = KeepOnUtils.getCurrentTimeout(this)
        val currentIndex = availableTimeout.indexOf(currentTimeout)
        val newTimeout = if (currentIndex == availableTimeout.size - 1 || currentIndex == -1) {
            availableTimeout[0]
        } else {
            availableTimeout[currentIndex + 1]
        }

        applyNewTimeout(newTimeout)
    }

    private fun applyNewTimeout(timeout: Int) {
        if (timeout == KeepOnUtils.getOriginalTimeout(this)) {
            KeepOnUtils.setKeepOn(false, this)
            if (KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, this))
                KeepOnUtils.stopScreenOffReceiverService(this)
        } else {
            KeepOnUtils.setKeepOn(true, this)
            if (!KeepOnUtils.isMyServiceRunning(ScreenOffReceiverService::class.java, this)
                && KeepOnUtils.getResetOnScreenOff(this)
            )
                KeepOnUtils.startScreenOffReceiverService(this)
        }

        KeepOnUtils.setTimeout(timeout, this)
    }
}