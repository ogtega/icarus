package de.tolunla.icarus.db.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Long, val name: String, @SerialName("screen_name") val username: String)
