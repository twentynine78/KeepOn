package fr.twentynine.keepon.ui.mapper

fun interface Mapper<in From, out To> {
    fun map(from: From): To
}
