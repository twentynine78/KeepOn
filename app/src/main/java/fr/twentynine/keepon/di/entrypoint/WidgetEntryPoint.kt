package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.ui.producer.WidgetStateProducer

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetStateProducer(): WidgetStateProducer
}
