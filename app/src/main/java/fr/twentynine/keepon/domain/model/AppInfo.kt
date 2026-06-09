package fr.twentynine.keepon.domain.model

/** Static app metadata shown on the About screen: version name, author and source-code URL. */
data class AppInfo(
    val version: String,
    val author: String,
    val sourceCodeUrl: String,
)
