package fr.twentynine.keepon.utils.glide

import android.graphics.Bitmap
import androidx.annotation.Nullable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class TimeoutIconModelLoader : ModelLoader<TimeoutIconData, Bitmap> {

    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    @Nullable
    override fun buildLoadData(model: TimeoutIconData, width: Int, height: Int, options: Options): LoadData<Bitmap> {
        return LoadData(ObjectKey(model), TimeoutIconDataFetcher(model))
    }

    override fun handles(model: TimeoutIconData): Boolean {
        return preferences.getTimeoutValueArray().contains(model.timeout)
    }
}
