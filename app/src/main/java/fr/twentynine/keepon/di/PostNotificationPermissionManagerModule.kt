package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.PostNotificationPermissionManagerImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class PostNotificationPermissionManagerModule {

    @Binds
    @ActivityScoped
    abstract fun bindPostNotificationPermissionManager(impl: PostNotificationPermissionManagerImpl): PostNotificationPermissionManager
}
