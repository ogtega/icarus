package de.tolunla.icarus.db

import androidx.room.TypeConverter
import de.tolunla.icarus.db.entity.User
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EntityConverters {
    private val formatter = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun stringFromUser(value: User?): String {
        return formatter.encodeToString(value)
    }

    @TypeConverter
    fun userFromString(str: String): User? {
        return formatter.decodeFromString(str)
    }

    @TypeConverter
    fun entitiesFromString(str: String): Map<String, List<String>> {
        return formatter.decodeFromString(str)
    }

    @TypeConverter
    fun stringFromEntities(str: Map<String, List<String>>): String {
        return formatter.encodeToString(str)
    }
}