package de.tolunla.icarus.db.entity

import androidx.room.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
@TypeConverters(Tweet.Converter::class)
@Entity(indices = [Index(value = ["id"], unique = true)])
data class Tweet(
    @PrimaryKey(autoGenerate = true) val key: Int? = null,
    val id: Long,
    val text: String,
    @SerialName("created_at") val createdAt: String,
    val source: String,
    val user: User,
    @SerialName("extended_entities") val entities: TweetEntities? = null
) {
    object Converter {
        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        @TypeConverter
        @JvmStatic
        fun serializeEntities(value: String?): TweetEntities? {
            return value?.let { jsonFormat.decodeFromString(it) }
        }

        @TypeConverter
        @JvmStatic
        fun deserializeEntities(entities: TweetEntities?): String {
            return jsonFormat.encodeToString(entities)
        }
    }
}
