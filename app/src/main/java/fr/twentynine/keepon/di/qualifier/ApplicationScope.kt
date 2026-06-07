package fr.twentynine.keepon.di.qualifier

import javax.inject.Qualifier

/**
 * Qualifies the application-lifetime [kotlinx.coroutines.CoroutineScope] (a singleton
 * backed by a [kotlinx.coroutines.SupervisorJob] on [kotlinx.coroutines.Dispatchers.Default]).
 * Injected wherever background work must outlive the calling component.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
