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
interface PermissionModule {

    @Binds
    @ActivityScoped
    fun bindBatteryOptimizationManager(impl: BatteryOptimizationManagerImpl): BatteryOptimizationManager

    @Binds
    @ActivityScoped
    fun bindPostNotificationPermissionManager(impl: PostNotificationPermissionManagerImpl): PostNotificationPermissionManager

    @Binds
    @ActivityScoped
    fun bindSystemSettingPermissionManager(impl: SystemSettingPermissionManagerImpl): SystemSettingPermissionManager
}
