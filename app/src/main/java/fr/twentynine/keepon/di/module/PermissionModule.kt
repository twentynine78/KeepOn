package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.permission.BatteryOptimizationManager
import fr.twentynine.keepon.util.permission.BatteryOptimizationManagerImpl
import fr.twentynine.keepon.util.permission.PostNotificationPermissionManager
import fr.twentynine.keepon.util.permission.PostNotificationPermissionManagerImpl
import fr.twentynine.keepon.util.permission.SystemSettingPermissionManager
import fr.twentynine.keepon.util.permission.SystemSettingPermissionManagerImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class PermissionModule {

    @Binds
    @ActivityScoped
    abstract fun bindBatteryOptimizationManager(impl: BatteryOptimizationManagerImpl): BatteryOptimizationManager

    @Binds
    @ActivityScoped
    abstract fun bindPostNotificationPermissionManager(impl: PostNotificationPermissionManagerImpl): PostNotificationPermissionManager

    @Binds
    @ActivityScoped
    abstract fun bindSystemSettingPermissionManager(impl: SystemSettingPermissionManagerImpl): SystemSettingPermissionManager
}
