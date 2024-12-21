package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.domain.model.DomainChat

fun RoomChat.toDomainModel() = DomainChat(
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

fun DomainChat.toRoomModel() = RoomChat(
    id = id!!,
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
