package fr.twentynine.keepon.core.coil

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.domain.model.TimeoutIconData

/**
 * Coil [Fetcher] that generates the timeout-icon bitmap from a [TimeoutIconData] instead of loading
 * it from disk/network: it draws the icon via [TimeoutIconGenerator] and returns it as an in-memory
 * [ImageFetchResult], so Coil's decode stage is bypassed entirely. The [Factory] is registered on the
 * app's [ImageLoader].
 */
class TimeoutIconDataFetcher(
    private val data: TimeoutIconData,
    private val options: Options,
    private val stringResourceProvider: StringResourceProvider,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return ImageFetchResult(
            image = TimeoutIconGenerator.getBitmapFromText(
                options.context,
                data,
                stringResourceProvider
            ).asImage(shareable = true),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory(
        private val stringResourceProvider: StringResourceProvider,
    ) : Fetcher.Factory<TimeoutIconData> {

        override fun create(data: TimeoutIconData, options: Options, imageLoader: ImageLoader): Fetcher {
            return TimeoutIconDataFetcher(data, options, stringResourceProvider)
        }
    }
}
