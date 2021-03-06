package fr.twentynine.keepon.utils.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class GlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSizeBytes = (1024 * 1024) * 2 // 2 MB

        builder
            .setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
            .setDefaultRequestOptions(
                RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(TimeoutIconData::class.java, Bitmap::class.java, TimeoutIconModelLoaderFactory())
    }
}
