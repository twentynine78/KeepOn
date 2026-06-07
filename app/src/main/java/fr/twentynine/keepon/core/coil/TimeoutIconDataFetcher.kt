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
