package illyan.butler.shared.llm

import com.aallam.openai.client.OpenAI
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.chat.SenderType
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val CONTENT_TYPE_IMAGE = "image"
private const val CONTENT_TYPE_AUDIO = "audio"
private const val RESOURCE_TYPE_AUDIO_MP3 = "audio/mp3"

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
actual fun provideLlmService(
    coroutineScopeIO: CoroutineScope,
    getResource: suspend (userId: Uuid, resourceId: Uuid) -> ResourceDto,
    createResource: suspend (userId: Uuid, chatId: Uuid, senderId: SenderType.Ai, resource: ResourceDto) -> ResourceDto,
    upsertMessage: suspend (userId: Uuid, message: MessageDto) -> MessageDto,
    getOpenAIClient: suspend (endpoint: String) -> OpenAI,
    upsertChat: suspend (chat: ChatDto) -> ChatDto,
    errorInMessageResponse: suspend (userId: Uuid, message: MessageDto?) -> Unit,
    removeMessage: suspend (userId: Uuid, message: MessageDto) -> Unit,
): LlmService? {
    return LlmService(
        coroutineScopeIO,
        getResource,
        createResource,
        upsertMessage,
        getOpenAIClient,
        upsertChat,
        errorInMessageResponse,
        removeMessage
    )
}
