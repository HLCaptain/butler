package illyan.butler.shared.model.chat

import illyan.butler.shared.model.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@Serializable
data class ChatDto(
    val id: Uuid,
    @Serializable(InstantSerializer::class)
    val createdAt: Instant,
    val name: String? = null,
    val ownerId: Uuid,
    val models: Map<Capability, AiSource> = emptyMap(),
    val summary: String? = null
)

@Serializable
enum class Capability {
    CHAT_COMPLETION,
    AUDIO_TRANSCRIPTION,
    AUDIO_TRANSLATION,
    IMAGE_GENERATION,
    SPEECH_SYNTHESIS
}
