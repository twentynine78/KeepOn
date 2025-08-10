package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import fr.twentynine.keepon.util.SystemSettingPermissionManager
import fr.twentynine.keepon.util.SystemSettingPermissionManagerImpl

@Module
@InstallIn(ActivityComponent::class)
object SystemSettingPermissionManagerModule {
    @Provides
    @ActivityScoped
    fun bindSystemSettingPermissionManager(@ActivityContext context: Context): SystemSettingPermissionManager {
        return SystemSettingPermissionManagerImpl(context)
    }
}
