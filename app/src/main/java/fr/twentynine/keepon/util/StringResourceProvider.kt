package fr.twentynine.keepon.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface StringResourceProvider {
    fun getString(resourceId: Int): String
    fun getPlural(resourceId: Int, count: Int): String
}

class StringResourceProviderImpl @Inject constructor(@param:ApplicationContext private val context: Context) : StringResourceProvider {
    private val resources by lazy { context.resources }

    override fun getString(resourceId: Int): String = resources.getString(resourceId)

    override fun getPlural(resourceId: Int, count: Int): String = resources.getQuantityString(resourceId, count, count)
}
