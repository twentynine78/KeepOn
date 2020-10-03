package fr.twentynine.keepon.glide

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.Nullable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import fr.twentynine.keepon.utils.KeepOnUtils


class TimeoutIconModelLoader(private val context: Context): ModelLoader<TimeoutIconData, Bitmap> {

    @Nullable
    override fun buildLoadData(model: TimeoutIconData, width: Int, height: Int, options: Options): LoadData<Bitmap>? {
        return LoadData(ObjectKey(model),  TimeoutIconDataFetcher(model, context))
    }

    override fun handles(model: TimeoutIconData): Boolean {
        return KeepOnUtils.getTimeoutValueArray().contains(model.timeout)
    }
}