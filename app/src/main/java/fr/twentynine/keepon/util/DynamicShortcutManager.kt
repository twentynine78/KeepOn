package fr.twentynine.keepon.util

import android.content.Context

object DynamicShortcutManager {
    fun removeAllDynamicShortcut(context: Context) {
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as android.content.pm.ShortcutManager
        val dynamicShortcutsId = shortcutManager.dynamicShortcuts.map { shortcut -> shortcut.id }

        if (dynamicShortcutsId.isNotEmpty()) {
            shortcutManager.removeDynamicShortcuts(dynamicShortcutsId)
        }
    }
}
