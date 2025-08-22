package fr.twentynine.keepon

import android.app.Application
import android.graphics.Bitmap
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.bitmapFactoryExifOrientationStrategy
import coil3.decode.ExifOrientationStrategy
import coil3.request.CachePolicy
import coil3.request.allowConversionToBitmap
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import coil3.size.Precision
import dagger.hilt.android.HiltAndroidApp
import fr.twentynine.keepon.util.AppVersionManager
import fr.twentynine.keepon.util.coil.TimeoutIconDataFetcher
import fr.twentynine.keepon.util.coil.TimeoutIconDataKeyer
import fr.twentynine.keepon.worker.MonitorSystemScreenTimeoutWorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltAndroidApp
class KeepOnApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider, CoroutineScope {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var appVersionManager: AppVersionManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val coroutineContext: CoroutineContext by lazy { applicationScope.coroutineContext }

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val workManager = WorkManager.getInstance(this@KeepOnApplication)
            MonitorSystemScreenTimeoutWorkScheduler.scheduleWork(workManager)

            appVersionManager.runAppMigrationIfNeeded()
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        applicationScope.cancel("Application is terminating")
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(this.applicationContext)
        .components {
            add(TimeoutIconDataKeyer())
            add(TimeoutIconDataFetcher.Factory())
        }
        .allowHardware(true)
        .allowConversionToBitmap(true)
        .bitmapConfig(Bitmap.Config.HARDWARE)
        .bitmapFactoryExifOrientationStrategy(ExifOrientationStrategy.IGNORE)
        .precision(Precision.INEXACT)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setWorkerCoroutineContext(Dispatchers.IO)
            .setDefaultProcessName(this.applicationInfo.processName)
            .build()
}
