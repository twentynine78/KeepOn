package fr.twentynine.keepon.services

import dagger.hilt.android.AndroidEntryPoint
import fr.twentynine.keepon.core.service.KeepOnTileServiceCore

/**
 * Quick Settings tile entry point.
 *
 * Kept at its original fully-qualified name (fr.twentynine.keepon.services.KeepOnTileService)
 * because the system persists this ComponentName for tiles the user already added; moving the
 * class would drop the tile from their Quick Settings. All behavior lives in [KeepOnTileServiceCore].
 */
@AndroidEntryPoint
class KeepOnTileService : KeepOnTileServiceCore() {

    override fun onCreate() {
        try {
            // The generated Hilt superclass injects before running KeepOnTileServiceCore.onCreate.
            super.onCreate()
        } catch (e: IllegalStateException) {
            // Only swallow Hilt's "must be attached to an @HiltAndroidApp Application" failure:
            // in restricted backup mode (auto-backup / device migration) Android launches the
            // process with the base Application class, and SystemUI may still bind the tile
            // there. Injection is impossible in that process, so the service stays inert instead
            // of crashing; the next bind in a healthy process works normally.
            if (e.message?.contains("Hilt") != true) throw e
            markInjectionFailed()
        }
    }
}
