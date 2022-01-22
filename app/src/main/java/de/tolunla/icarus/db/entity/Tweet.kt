package de.tolunla.icarus.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Tweet(
    @PrimaryKey val id: Long,
    val text: String,
    @SerialName("created_at") val createdAt: String,
    val source: String,
    val user: User,
)
