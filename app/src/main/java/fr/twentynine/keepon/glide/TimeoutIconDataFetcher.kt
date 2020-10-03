package fr.twentynine.keepon.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import fr.twentynine.keepon.utils.KeepOnUtils

class TimeoutIconDataFetcher(private val model: TimeoutIconData, private val context: Context) : DataFetcher<Bitmap> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        if (model.size != 3) {
            callback.onDataReady(KeepOnUtils.getBitmapFromText(model.timeout, context, model.size == 1))
        } else {
            callback.onDataReady(KeepOnUtils.getShortcutBitmapFromText(model.timeout, context))
        }
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    override fun cleanup() {}
    override fun cancel() {}
}
