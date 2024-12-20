package illyan.butler.core.network.mapping

import illyan.butler.domain.model.DomainChat
import illyan.butler.shared.model.chat.ChatDto

fun ChatDto.toDomainModel() = DomainChat(
    id = id,
    created = created,
    name = name,
    ownerId = ownerId,
    chatCompletionModel = chatCompletionModel,
    audioTranscriptionModel = audioTranscriptionModel,
    audioTranslationModel = audioTranslationModel,
    imageGenerationsModel = imageGenerationsModel,
    audioSpeechModel = audioSpeechModel,
    summary = summary,
)

fun DomainChat.toNetworkModel() = ChatDto(
    id = id,
    created = created,
    name = name,
    ownerId = ownerId,
    chatCompletionModel = chatCompletionModel,
    audioTranscriptionModel = audioTranscriptionModel,
    audioTranslationModel = audioTranslationModel,
    imageGenerationsModel = imageGenerationsModel,
    audioSpeechModel = audioSpeechModel,
    summary = summary,
)
