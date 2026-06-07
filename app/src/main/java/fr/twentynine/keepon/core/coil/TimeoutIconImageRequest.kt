package fr.twentynine.keepon.core.coil

import android.content.Context
import coil3.request.ImageRequest
import coil3.size.Size
import fr.twentynine.keepon.domain.model.TimeoutIconData

/**
 * Builds the Coil request for a generated timeout icon. The size is pinned to
 * [Size.ORIGINAL] because the bitmap is rendered at a fixed size by
 * [TimeoutIconDataFetcher], independent of where it is displayed — this keeps a
 * single memory-cache entry per icon regardless of the display size.
 */
fun timeoutIconImageRequest(context: Context, data: TimeoutIconData): ImageRequest =
    ImageRequest.Builder(context)
        .data(data)
        .size(Size.ORIGINAL)
        .build()
