@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Capability
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.Source
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun ChatDto.toDomainModel(source: Source) = Chat(
    id = id,
    createdAt = createdAt,
    title = name,
    source = source,
    models = models.toDomainModel(),
    summary = summary,
)

fun Chat.toNetworkModel() = ChatDto(
    id = id,
    createdAt = createdAt,
    name = title,
    ownerId = when (source) {
        is Source.Device -> (source as Source.Device).deviceId
        is Source.Server -> (source as Source.Server).userId
    },
    summary = summary,
    models = models.toNetworkModel()
)

fun Map<Capability, AiSource>.toNetworkModel(): Map<illyan.butler.shared.model.chat.Capability, AiSource> {
    return map { (key, value) ->
        key.toNetworkModel() to value
    }.toMap()
}

fun Map<illyan.butler.shared.model.chat.Capability, AiSource>.toDomainModel(): Map<Capability, AiSource> {
    return map { (key, value) ->
        key.toDomainModel() to value
    }.toMap()
}

fun Capability.toNetworkModel() = when (this) {
    Capability.CHAT_COMPLETION -> illyan.butler.shared.model.chat.Capability.CHAT_COMPLETION
    Capability.IMAGE_GENERATION -> illyan.butler.shared.model.chat.Capability.IMAGE_GENERATION
    Capability.SPEECH_SYNTHESIS -> illyan.butler.shared.model.chat.Capability.SPEECH_SYNTHESIS
    Capability.AUDIO_TRANSCRIPTION -> illyan.butler.shared.model.chat.Capability.AUDIO_TRANSCRIPTION
    Capability.AUDIO_TRANSLATION -> illyan.butler.shared.model.chat.Capability.AUDIO_TRANSLATION
}

fun illyan.butler.shared.model.chat.Capability.toDomainModel() = when (this) {
    illyan.butler.shared.model.chat.Capability.CHAT_COMPLETION -> Capability.CHAT_COMPLETION
    illyan.butler.shared.model.chat.Capability.IMAGE_GENERATION -> Capability.IMAGE_GENERATION
    illyan.butler.shared.model.chat.Capability.SPEECH_SYNTHESIS -> Capability.SPEECH_SYNTHESIS
    illyan.butler.shared.model.chat.Capability.AUDIO_TRANSCRIPTION -> Capability.AUDIO_TRANSCRIPTION
    illyan.butler.shared.model.chat.Capability.AUDIO_TRANSLATION -> Capability.AUDIO_TRANSLATION
}
