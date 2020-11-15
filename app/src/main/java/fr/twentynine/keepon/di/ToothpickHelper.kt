package fr.twentynine.keepon.di

import android.app.Application
import android.app.Service
import android.content.ContentResolver
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.RequestManager
import fr.twentynine.keepon.di.annotation.ActivityScope
import fr.twentynine.keepon.di.annotation.ApplicationScope
import fr.twentynine.keepon.di.annotation.ServiceScope
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.glide.GlideApp
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.smoothie.lifecycle.closeOnDestroy

object ToothpickHelper {

    fun scopedInjection(component: Any) {
        when (component) {
            is Application -> {
                // Install Toothpick Application module in Application scope
                KTP.openScope(ApplicationScope::class.java)
                    .installModules(module {
                        bind<Application>().toInstance(component)
                        bind<ContentResolver>().toInstance(component.contentResolver)
                        bind<RequestManager>().toProviderInstance { GlideApp.with(component.applicationContext) }.providesSingleton()
                    })
                    .closeOnDestroy(ProcessLifecycleOwner.get())
            }
            is AppCompatActivity -> {
                // Install Toothpick Activity module in Activity sub scope and inject
                KTP.openScope(ApplicationScope::class.java)
                    .openSubScope(ActivityScope::class.java)
                    .openSubScope(component)
                    .supportScopeAnnotation(ActivityScope::class.java)
                    .installModules(module {
                        bind<AppCompatActivity>().toInstance(component)
                        bind<RequestManager>().toInstance(GlideApp.with(component))
                    })
                    .closeOnDestroy(component)
                    .inject(component)
            }
            is Fragment -> {
                // Open scope of parent activity and inject
                KTP.openScope(ApplicationScope::class.java)
                    .openSubScope(ActivityScope::class.java)
                    .openSubScope(component.activity)
                    .inject(component)
            }
            is KeepOnTileService -> {
                // Open Application sub scope and inject
                KTP.openScope(ApplicationScope::class.java)
                    .openSubScope(component)
                    .installModules(module {
                        bind<RequestManager>().toInstance(GlideApp.with(component))
                    })
                    .closeOnDestroy(component)
                    .inject(component)
            }
            is LifecycleService -> {
                // Install Toothpick Service module in Service sub scope and inject
                KTP.openScope(ApplicationScope::class.java)
                    .openSubScope(ServiceScope::class.java)
                    .openSubScope(component)
                    .supportScopeAnnotation(ServiceScope::class.java)
                    .installModules(module {
                        bind<Service>().toInstance(component)
                    })
                    .closeOnDestroy(component)
                    .inject(component)
            }
            else -> {
                // Open Application scope and inject
                KTP.openScope(ApplicationScope::class.java)
                    .inject(component)
            }
        }
    }
}
