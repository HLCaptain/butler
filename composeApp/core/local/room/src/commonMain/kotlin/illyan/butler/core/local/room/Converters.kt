package illyan.butler.core.local.room

import androidx.room.TypeConverter
import illyan.butler.core.local.room.model.RoomAddress
import illyan.butler.core.local.room.model.RoomPreferences
import illyan.butler.core.local.room.model.RoomToken
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(databaseValue: String): List<String> {
        if (databaseValue.isEmpty()) return emptyList()
        return databaseValue.split(",")
    }

    @TypeConverter
    fun toString(map: Map<String, String>): String {
        return map.map { "${it.key}=${it.value}" }.joinToString(",")
    }

    @TypeConverter
    fun toMap(databaseValue: String): Map<String, String> {
        if (databaseValue.isEmpty()) return emptyMap()
        return databaseValue.split(",").filter { it.isNotBlank() }.associate {
            val (key, value) = it.split("=")
            key to value
        }
    }

    @TypeConverter
    fun toString(preferences: RoomPreferences): String {
        return Json.encodeToString(RoomPreferences.serializer(), preferences)
    }

    @TypeConverter
    fun toDomainPreferences(databaseValue: String): RoomPreferences {
        return Json.decodeFromString(RoomPreferences.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(token: RoomToken): String {
        return Json.encodeToString(RoomToken.serializer(), token)
    }

    @TypeConverter
    fun toDomainToken(databaseValue: String): RoomToken {
        return Json.decodeFromString(RoomToken.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(address: RoomAddress): String {
        return Json.encodeToString(RoomAddress.serializer(), address)
    }

    @TypeConverter
    fun toDomainAddress(databaseValue: String): RoomAddress {
        return Json.decodeFromString(RoomAddress.serializer(), databaseValue)
    }
}
