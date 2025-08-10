package fr.twentynine.keepon.util.extensions

import android.content.res.Resources

// Extension function to convert dp to px
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)
