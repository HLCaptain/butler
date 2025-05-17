package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String? = null,
    val created: Long? = null,
    val name: String? = null,
    val ownerId: String,
    val models: Map<Capability, ModelConfig> = emptyMap(),
    val summary: String? = null
)

enum class Capability {
    CHAT_COMPLETION,
    AUDIO_TRANSCRIPTION,
    AUDIO_TRANSLATION,
    IMAGE_GENERATION,
    SPEECH_SYNTHESIS
}

@Serializable
data class ModelConfig(
    val endpoint: String,        // e.g. https://api.openai.com/v1/
    val modelId: String          // e.g. gpt-4o
)

// Model endpoint compatibility
// Endpoint                 Latest models
// /v1/assistants           All GPT-4o (except chatgpt-4o-latest), GPT-4o-mini, GPT-4, and GPT-3.5 Turbo models.
//                          The retrieval tool requires gpt-4-turbo-preview (and subsequent dated model releases)
//                          or gpt-3.5-turbo-1106 (and subsequent versions).
// /v1/audio/transcriptions whisper-1
// /v1/audio/translations   whisper-1
// /v1/audio/speech         tts-1, tts-1-hd
// /v1/chat/completions     All GPT-4o (except for Realtime preview), GPT-4o-mini, GPT-4, and GPT-3.5 Turbo models
//                          and their dated releases. chatgpt-4o-latest dynamic model. Fine-tuned versions of gpt-4o,
//                          gpt-4o-mini, gpt-4, and gpt-3.5-turbo.
// /v1/completions (Legacy) gpt-3.5-turbo-instruct, babbage-002, davinci-002
// /v1/embeddings           text-embedding-3-small, text-embedding-3-large, text-embedding-ada-002
// /v1/fine_tuning/jobs     gpt-4o, gpt-4o-mini, gpt-4, gpt-3.5-turbo
// /v1/moderations          text-moderation-stable, text-moderation-latest
// /v1/images/generations   dall-e-2, dall-e-3
// /v1/realtime (beta)      gpt-4o-realtime-preview, gpt-4o-realtime-preview-2024-10-01
