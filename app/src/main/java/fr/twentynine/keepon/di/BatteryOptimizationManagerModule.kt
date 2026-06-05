package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.BatteryOptimizationManager
import fr.twentynine.keepon.util.BatteryOptimizationManagerImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class BatteryOptimizationManagerModule {

    @Binds
    @ActivityScoped
    abstract fun bindBatteryOptimizationManager(impl: BatteryOptimizationManagerImpl): BatteryOptimizationManager
}
