package illyan.butler.core.local.room

import androidx.room.TypeConverter
import illyan.butler.core.local.room.model.RoomAddress
import illyan.butler.core.local.room.model.RoomPreferences
import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.FilterOption
import illyan.butler.shared.model.chat.MessageStatus
import illyan.butler.shared.model.chat.PromptConfiguration
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
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
        return Json.encodeToString(preferences)
    }

    @TypeConverter
    fun toDomainPreferences(databaseValue: String): RoomPreferences {
        return Json.decodeFromString(RoomPreferences.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(token: RoomToken): String {
        return Json.encodeToString(token)
    }

    @TypeConverter
    fun toDomainToken(databaseValue: String): RoomToken {
        return Json.decodeFromString(RoomToken.serializer(), databaseValue)
    }

    @TypeConverter
    fun toString(address: RoomAddress): String {
        return Json.encodeToString(address)
    }

    @TypeConverter
    fun toDomainAddress(databaseValue: String): RoomAddress {
        return Json.decodeFromString(RoomAddress.serializer(), databaseValue)
    }

    @TypeConverter
    fun toByteArray(databaseValue: Uuid) = databaseValue.toByteArray()
    @TypeConverter
    fun toUuid(databaseValue: ByteArray) = Uuid.fromByteArray(databaseValue)

    @TypeConverter
    fun toLong(value: Instant) = value.toEpochMilliseconds()
    @TypeConverter
    fun toInstant(value: Long) = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun toString(value: SenderType) = Json.encodeToString(value)
    @TypeConverter
    fun toSenderType(databaseValue: String) = Json.decodeFromString(SenderType.serializer(), databaseValue)

    @TypeConverter
    fun toInt(messageStatus: MessageStatus) = messageStatus.ordinal
    @TypeConverter
    fun toMessageStatus(databaseValue: Int) = MessageStatus.entries[databaseValue]

    @TypeConverter
    fun toString(source: Source) = Json.encodeToString(source)
    @TypeConverter
    fun toSource(databaseValue: String) = Json.decodeFromString(Source.serializer(), databaseValue)

    @Serializable
    data class StringPair(val first: String, val second: String)

    @TypeConverter
    fun toString(pair: Pair<String, String>) = Json.encodeToString(pair.toStringPair())
    @TypeConverter
    fun toPair(databaseValue: String) = Json.decodeFromString(StringPair.serializer(), databaseValue).toPair()

    val structuredKeyMapConverter = Json { allowStructuredMapKeys = true }
    @TypeConverter
    fun toString(filters: Set<FilterOption>) = structuredKeyMapConverter.encodeToString(filters)
    @TypeConverter
    fun toFilterSet(databaseValue: String) = structuredKeyMapConverter.decodeFromString(SetSerializer(FilterOption.serializer()), databaseValue)

    @JvmName("promptConfigurationListToString")
    @TypeConverter
    fun toString(promptConfigurations: List<PromptConfiguration>) = Json.encodeToString(ListSerializer(PromptConfiguration.serializer()), promptConfigurations)
    @TypeConverter
    fun toPromptConfigurationList(databaseValue: String) = Json.decodeFromString(ListSerializer(PromptConfiguration.serializer()), databaseValue)

    @JvmName("modelsMapToString")
    @TypeConverter
    fun toString(models: Map<Capability, AiSource>) = Json.encodeToString(MapSerializer(Capability.serializer(), AiSource.serializer()), models)
    @TypeConverter
    fun toModelsMap(databaseValue: String) = Json.decodeFromString(MapSerializer(Capability.serializer(), AiSource.serializer()), databaseValue)
}

private fun Pair<String, String>.toStringPair(): Converters.StringPair {
    return Converters.StringPair(first, second)
}

private fun Converters.StringPair.toPair(): Pair<String, String> {
    return first to second
}
