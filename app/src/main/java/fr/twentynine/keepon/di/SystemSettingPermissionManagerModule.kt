package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import fr.twentynine.keepon.util.SystemSettingPermissionManagerImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class SystemSettingPermissionManagerModule {

    @Binds
    @ActivityScoped
    abstract fun bindSystemSettingPermissionManager(impl: SystemSettingPermissionManagerImpl): SystemSettingPermissionManager
}
