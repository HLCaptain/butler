package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Capability
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.ModelConfig
import illyan.butler.shared.model.chat.ChatDto

fun ChatDto.toDomainModel() = DomainChat(
    id = id,
    created = created,
    name = name,
    ownerId = ownerId,
    models = models.toDomainModel(),
    summary = summary,
)

fun DomainChat.toNetworkModel() = ChatDto(
    id = id,
    created = created,
    name = name,
    ownerId = ownerId,
    models = models.toNetworkModel(),
    summary = summary,
)

fun Map<Capability, ModelConfig>.toNetworkModel(): Map<illyan.butler.shared.model.chat.Capability, illyan.butler.shared.model.chat.ModelConfig> {
    return map { (key, value) ->
        key.toNetworkModel() to value.toNetworkModel()
    }.toMap()
}

fun Map<illyan.butler.shared.model.chat.Capability, illyan.butler.shared.model.chat.ModelConfig>.toDomainModel(): Map<Capability, ModelConfig> {
    return map { (key, value) ->
        key.toDomainModel() to value.toDomainModel()
    }.toMap()
}

fun ModelConfig.toNetworkModel() = illyan.butler.shared.model.chat.ModelConfig(
    modelId = modelId,
    endpoint = endpoint
)

fun illyan.butler.shared.model.chat.ModelConfig.toDomainModel() = ModelConfig(
    modelId = modelId,
    endpoint = endpoint
)

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
