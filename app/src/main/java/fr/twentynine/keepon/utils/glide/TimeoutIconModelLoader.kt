package fr.twentynine.keepon.utils.glide

import android.graphics.Bitmap
import androidx.annotation.Nullable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey

class TimeoutIconModelLoader : ModelLoader<TimeoutIconData, Bitmap> {

    @Nullable
    override fun buildLoadData(model: TimeoutIconData, width: Int, height: Int, options: Options): LoadData<Bitmap> {
        return LoadData(ObjectKey(model), TimeoutIconDataFetcher(model))
    }

    override fun handles(model: TimeoutIconData): Boolean {
        return true
    }
}
