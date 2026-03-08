package fr.twentynine.keepon.util.extensions

import android.content.Context
import android.graphics.Bitmap
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.model.WidgetUIState

suspend fun WidgetUIState.getContentBitmap(
    context: Context,
): Bitmap? {
    if (this !is WidgetUIState.Success) {
        return null
    }

    val request = ImageRequest.Builder(context)
        .data(
            TimeoutIconData(
                currentScreenTimeout,
                TimeoutIconSize.LARGE,
                this.timeoutIconStyle
            )
        )
        .build()

    return when (val result = context.imageLoader.execute(request)) {
        is SuccessResult -> {
            result.image.toBitmap()
        }
        else -> {
            null
        }
    }
}
