package fr.twentynine.keepon

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Process
import android.service.quicksettings.TileService
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("unused")
class KeepOnApplication : Application() {

    private val preferences: Preferences by lazy()

    override fun onCreate() {
        super.onCreate()
        // Install Toothpick Application module in Application scope
        ToothpickHelper.scopedInjection(this)

        if (!getCurrentProcessName().contains(':')) {
            preferences.setAppILaunched(false)
            TileService.requestListeningState(this, ComponentName(this, KeepOnTileService::class.java))
        }
    }

    private fun getCurrentProcessName(): String {
        val pid = Process.myPid()
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                return processInfo.processName
            }
        }
        return ""
    }

    companion object {
        // Helper function for Activity ViewBinding
        inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
            crossinline bindingInflater: (LayoutInflater) -> T
        ): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
            bindingInflater.invoke(layoutInflater)
        }
        // Helper function for Fragment ViewBinding
        fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
            FragmentViewBindingDelegate(this, viewBindingFactory)
    }

    // Helper Class for Fragment ViewBinding
    class FragmentViewBindingDelegate<T : ViewBinding>(val fragment: Fragment, val viewBindingFactory: (View) -> T) : ReadOnlyProperty<Fragment, T> {
        private var binding: T? = null
        init {
            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                            override fun onDestroy(owner: LifecycleOwner) {
                                binding = null
                            }
                        })
                    }
                }
            })
        }
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
            val binding = binding
            if (binding != null) {
                return binding
            }

            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
            }

            return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
        }
    }
}
