package fr.twentynine.keepon.core.util

import android.content.res.Resources

/** This dp value converted to pixels using the system display density. */
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/** This dp value converted to pixels using the system display density. */
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)
