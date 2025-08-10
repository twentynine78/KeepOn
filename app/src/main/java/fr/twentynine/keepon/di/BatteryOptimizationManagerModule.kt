package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.BatteryOptimizationManagerImpl

@Module
@InstallIn(ActivityComponent::class)
object BatteryOptimizationManagerModule {
    @Provides
    @ActivityScoped
    fun provideBatteryOptimizationManager(@ActivityContext context: Context): BatteryOptimizationManager {
        return BatteryOptimizationManagerImpl(context)
    }
}
