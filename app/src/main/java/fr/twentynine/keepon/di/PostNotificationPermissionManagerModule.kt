package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.PostNotificationPermissionManager
import fr.twentynine.keepon.util.PostNotificationPermissionManagerImpl

@Module
@InstallIn(ActivityComponent::class)
object PostNotificationPermissionManagerModule {
    @Provides
    @ActivityScoped
    fun bindPostNotificationPermissionManager(@ActivityContext context: Context): PostNotificationPermissionManager {
        return PostNotificationPermissionManagerImpl(context)
    }
}
