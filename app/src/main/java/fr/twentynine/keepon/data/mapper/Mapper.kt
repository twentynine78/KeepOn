package fr.twentynine.keepon.data.mapper

fun interface Mapper<in From, out To> {
    fun map(from: From): To
}
