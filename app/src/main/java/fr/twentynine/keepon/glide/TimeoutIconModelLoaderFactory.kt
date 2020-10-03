package fr.twentynine.keepon.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class TimeoutIconModelLoaderFactory(private val context: Context) : ModelLoaderFactory<TimeoutIconData, Bitmap> {
    override fun build(unused: MultiModelLoaderFactory): ModelLoader<TimeoutIconData, Bitmap> {
        return TimeoutIconModelLoader(context)
    }

    override fun teardown() {}
}
