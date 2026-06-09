package fr.twentynine.keepon.core.system

import android.content.Context
import android.content.pm.ShortcutManager
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.DynamicShortcutManager
import javax.inject.Inject

/** Clears the app's launcher dynamic shortcuts via the system [ShortcutManager]. */
class DynamicShortcutManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DynamicShortcutManager {

    override fun removeAllDynamicShortcuts() {
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
        val dynamicShortcutsId = shortcutManager.dynamicShortcuts.map { shortcut -> shortcut.id }

        if (dynamicShortcutsId.isNotEmpty()) {
            shortcutManager.removeDynamicShortcuts(dynamicShortcutsId)
        }
    }
}
