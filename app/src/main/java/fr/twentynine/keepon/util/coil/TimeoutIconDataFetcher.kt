package fr.twentynine.keepon.util.coil

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.util.StringResourceProvider

class TimeoutIconDataFetcher(
    private val data: TimeoutIconData,
    private val options: Options
) : Fetcher {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TimeoutIconDataFetcherEntryPoint {
        fun stringResourceProvider(): StringResourceProvider
    }

    override suspend fun fetch(): FetchResult {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            options.context,
            TimeoutIconDataFetcherEntryPoint::class.java
        )
        val stringResourceProvider = hiltEntryPoint.stringResourceProvider()

        return ImageFetchResult(
            image = TimeoutIconGenerator().getBitmapFromText(
                options.context,
                data,
                stringResourceProvider
            ).asImage(shareable = true),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<TimeoutIconData> {

        override fun create(data: TimeoutIconData, options: Options, imageLoader: ImageLoader): Fetcher {
            return TimeoutIconDataFetcher(data, options)
        }
    }
}
