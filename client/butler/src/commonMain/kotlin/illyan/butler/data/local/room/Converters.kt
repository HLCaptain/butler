package illyan.butler.data.local.room

import androidx.room.TypeConverter
import illyan.butler.domain.model.DomainAddress
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.DomainToken
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
    fun toString(preferences: DomainPreferences): String {
        return Json.encodeToString(DomainPreferences.serializer(), preferences)
    }

    @TypeConverter
    fun toDomainPreferences(databaseValue: String): DomainPreferences {
        return Json.decodeFromString(DomainPreferences.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(token: DomainToken): String {
        return Json.encodeToString(DomainToken.serializer(), token)
    }

    @TypeConverter
    fun toDomainToken(databaseValue: String): DomainToken {
        return Json.decodeFromString(DomainToken.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(address: DomainAddress): String {
        return Json.encodeToString(DomainAddress.serializer(), address)
    }

    @TypeConverter
    fun toDomainAddress(databaseValue: String): DomainAddress {
        return Json.decodeFromString(DomainAddress.serializer(), databaseValue)
    }
}