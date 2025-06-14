package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable

@Serializable
sealed interface AiSource {
    val modelId: String
    val endpoint: String
        get() = when (this) {
            is Api -> endpoint
            is Server -> source.endpoint
            is Local -> throw IllegalArgumentException("Unsupported AiSource type: Local")
        }

    data class Local(override val modelId: String) : AiSource

    data class Api(
        override val modelId: String, // e.g. "gpt-3.5-turbo"
        override val endpoint: String, // e.g. "https://api.openai.com/v1/"
        val type: ApiType = ApiType.OPENAI, // default to OpenAI
    ) : AiSource

    data class Server(
        override val modelId: String, // e.g. "gpt-3.5-turbo"
        val source: AiSource
    ) : AiSource
}
