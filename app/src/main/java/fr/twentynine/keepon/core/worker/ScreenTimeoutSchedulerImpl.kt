package fr.twentynine.keepon.core.worker

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.domain.gateway.ScreenTimeoutScheduler
import javax.inject.Inject

/** Implements [ScreenTimeoutScheduler] by enqueuing [SetNewScreenTimeoutWork] through WorkManager. */
class ScreenTimeoutSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ScreenTimeoutScheduler {

    override fun schedule(newScreenTimeout: Int, updatePreviousTimeout: Boolean) {
        SetNewScreenTimeoutWorkScheduler().scheduleWork(newScreenTimeout, context, updatePreviousTimeout)
    }
}
