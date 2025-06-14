package illyan.butler.core.local.room

import androidx.room.TypeConverter
import illyan.butler.core.local.room.model.RoomAddress
import illyan.butler.core.local.room.model.RoomPreferences
import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.domain.model.AiSource
import illyan.butler.domain.model.Capability
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    @OptIn(ExperimentalUuidApi::class)
    @TypeConverter
    fun toByteArray(databaseValue: Uuid): ByteArray {
        return databaseValue.toByteArray()
    }

    @OptIn(ExperimentalUuidApi::class)
    @TypeConverter
    fun toUuid(databaseValue: ByteArray): Uuid {
        return Uuid.fromByteArray(databaseValue)
    }

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    fun toLong(value: Instant): Long {
        return value.toEpochMilliseconds()
    }

    @OptIn(ExperimentalTime::class)
    @TypeConverter
    fun toInstant(value: Long): Instant {
        return Instant.fromEpochMilliseconds(value)
    }

    @Serializable
    data class StringPair(val first: String, val second: String)

    @TypeConverter
    fun toString(pair: Pair<String, String>): String {
        return Json.encodeToString(StringPair.serializer(), pair.toStringPair())
    }

    @TypeConverter
    fun toPair(databaseValue: String): Pair<String, String> {
        return Json.decodeFromString(StringPair.serializer(), databaseValue).toPair()
    }

    @JvmName("modelsMapToString")
    @TypeConverter
    fun toString(models: Map<Capability, AiSource>): String {
        return Json.encodeToString(MapSerializer(Capability.serializer(), AiSource.serializer()), models)
    }

    @TypeConverter
    fun toModelsMap(databaseValue: String): Map<Capability, AiSource> {
        return Json.decodeFromString(MapSerializer(Capability.serializer(), AiSource.serializer()), databaseValue)
    }
}

private fun Pair<String, String>.toStringPair(): Converters.StringPair {
    return Converters.StringPair(first, second)
}

private fun Converters.StringPair.toPair(): Pair<String, String> {
    return first to second
}
