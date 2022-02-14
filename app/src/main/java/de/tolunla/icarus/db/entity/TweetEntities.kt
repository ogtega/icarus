package de.tolunla.icarus.db.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TweetEntities(
    val media: List<TweetMedia> = emptyList()
) {
    @Serializable
    data class TweetMedia(
        val id: Long,
        @SerialName("media_url_https") val url: String,
        val type: String,
    )
}
