package de.tolunla.icarus.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(indices = [Index(value = ["id"], unique = true)])
data class Tweet(
    @PrimaryKey(autoGenerate = true) val key: Int? = null,
    val id: Long,
    val text: String,
    @SerialName("created_at") val createdAt: String,
    val source: String,
    val user: User,
)
