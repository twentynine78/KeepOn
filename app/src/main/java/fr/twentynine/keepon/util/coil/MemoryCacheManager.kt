package fr.twentynine.keepon.util.coil

import android.content.Context
import coil3.imageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface MemoryCacheManager {
    fun clear()
}

class MemoryCacheManagerImpl @Inject constructor(@param:ApplicationContext private val context: Context) : MemoryCacheManager {
    override fun clear() {
        context.imageLoader.memoryCache?.clear()
    }
}
