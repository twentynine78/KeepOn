package fr.twentynine.keepon.utils

import android.content.res.Resources
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

// Helper function to convert px to dp
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

// Helper function for Activity ViewBinding
inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}
