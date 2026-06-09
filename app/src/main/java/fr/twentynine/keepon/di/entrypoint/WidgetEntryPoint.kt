package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.ui.producer.WidgetStateProducer

/** Hilt entry point letting the Glance widget (not a Hilt-injected class) reach the widget state producer. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetStateProducer(): WidgetStateProducer
}
