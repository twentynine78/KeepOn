package fr.twentynine.keepon.ui.mapper

/** A one-way conversion from [From] to [To]; the shared shape for the UI-layer mappers. */
fun interface Mapper<in From, out To> {
    fun map(from: From): To
}
