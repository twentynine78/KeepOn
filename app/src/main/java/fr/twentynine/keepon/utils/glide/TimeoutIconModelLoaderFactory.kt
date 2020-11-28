package fr.twentynine.keepon.utils.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class TimeoutIconModelLoaderFactory : ModelLoaderFactory<TimeoutIconData, Bitmap> {

    override fun build(unused: MultiModelLoaderFactory): ModelLoader<TimeoutIconData, Bitmap> {
        return TimeoutIconModelLoader()
    }

    override fun teardown() {}
}
