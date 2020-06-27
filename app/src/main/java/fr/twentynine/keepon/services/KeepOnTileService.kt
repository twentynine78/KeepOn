package fr.twentynine.keepon.services

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import fr.twentynine.keepon.R
import fr.twentynine.keepon.SplashScreen
import fr.twentynine.keepon.utils.KeepOnUtils


class KeepOnTileService : TileService() {

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

        val keeponTile = qsTile

        if (qsTile.state == Tile.STATE_UNAVAILABLE) this.stopSelf()

        val newTimeout = if (KeepOnUtils.getNewTimeout(this) >= 0) {
            KeepOnUtils.getNewTimeout(this)
        } else {
            KeepOnUtils.getCurrentTimeout(this)
        }
        KeepOnUtils.setNewTimeout(-1, this)

        val originalTimeout = KeepOnUtils.getOriginalTimeout(this)
        val tileState = if (newTimeout == originalTimeout) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        val tileIcon = Icon.createWithBitmap(KeepOnUtils.getBitmapFromText(newTimeout, this))

        if (keeponTile != null && tileIcon != null) {
            keeponTile.state = tileState
            keeponTile.icon = tileIcon
            keeponTile.label = getString(R.string.qs_tile_label)
            keeponTile.contentDescription = getString(R.string.qs_tile_desc)

            keeponTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        KeepOnUtils.setTileAdded(true, this)

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

        KeepOnUtils.setNewTimeout(newTimeout, this)
        requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
        applyNewTimeout(newTimeout)
    }

    private fun applyNewTimeout(timeout: Int) {
        if (timeout == KeepOnUtils.getOriginalTimeout(this)) {
            KeepOnUtils.setKeepOn(false, this)
            KeepOnUtils.stopScreenOffReceiverService(this)
        } else {
            KeepOnUtils.setKeepOn(true, this)
            if (KeepOnUtils.getResetOnScreenOff(this))
                KeepOnUtils.startScreenOffReceiverService(this)
        }

        KeepOnUtils.setTimeout(timeout, this)
    }
}