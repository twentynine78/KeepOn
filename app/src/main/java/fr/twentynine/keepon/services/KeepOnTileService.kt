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
class KeepOnTileService : KeepOnTileServiceCore()
