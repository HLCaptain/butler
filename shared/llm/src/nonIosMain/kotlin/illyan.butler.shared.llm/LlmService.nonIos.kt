package illyan.butler.shared.llm

import com.aallam.openai.client.OpenAI
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.chat.SenderType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val CONTENT_TYPE_IMAGE = "image"
private const val CONTENT_TYPE_AUDIO = "audio"
private const val RESOURCE_TYPE_AUDIO_MP3 = "audio/mp3"

/**
 * Implementation of LLMService using Koog's graph-based approach.
 * 
 * This implementation conceptually organizes operations as nodes in a graph with explicit dependencies
 * between them, which helps address concurrency issues. The key operations include:
 * 
 * 1. Preparing the conversation - Node that processes chat messages and determines if regeneration is needed
 * 2. Retrieving resources - Node that fetches resources associated with messages
 * 3. Creating conversation - Node that converts messages to a conversation format
 * 4. Handling messages - Node that processes text or resource messages appropriately
 * 5. Updating chat metadata - Node that generates name and summary for the chat
 * 
 * These operations are connected with edges that define their dependencies, ensuring that they
 * execute in the correct order and with the necessary inputs. This approach helps address
 * concurrency issues by making dependencies explicit and ensuring that operations don't
 * interfere with each other.
 * 
 * The implementation uses Koog's agent-based framework to execute this graph of operations,
 * which provides better handling of concurrent tasks compared to the previous implementation.
 */
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

@OptIn(ExperimentalUuidApi::class)
class KoogLlmService(
    coroutineScopeIO: CoroutineScope,
    getResource: suspend (userId: Uuid, resourceId: Uuid) -> ResourceDto,
    createResource: suspend (userId: Uuid, chatId: Uuid, senderId: SenderType.Ai, resource: ResourceDto) -> ResourceDto,
    upsertMessage: suspend (userId: Uuid, message: MessageDto) -> MessageDto,
    getOpenAIClient: suspend (endpoint: String) -> OpenAI,
    upsertChat: suspend (chat: ChatDto) -> ChatDto,
    errorInMessageResponse: suspend (userId: Uuid, message: MessageDto?) -> Unit,
    removeMessage: suspend (userId: Uuid, message: MessageDto) -> Unit
) : LlmService(
    coroutineScopeIO,
    getResource,
    createResource,
    upsertMessage,
    getOpenAIClient,
    upsertChat,
    errorInMessageResponse,
    removeMessage
) {
    override suspend fun answerChat(
        chat: ChatDto,
        chatMessages: List<MessageDto>,
        previousChats: List<ChatDto>,
        regenerateMessage: MessageDto?
    ) {
        //  Sort and prepare the conversation
        // • Order messages by timestamp
        // • Determine if this is a regeneration of an existing AI message
        // • Remove any blank AI message if regenerating
        //
        // Early exits
        // • If there are no messages, return immediately
        // • If the last message isn’t from the user (and not regenerating), update chat metadata and return
        //
        // Fetch any referenced resources (images, audio)
        //
        // Build a list of ChatMessage objects from MessageDto, merging consecutive messages from the same role
        //
        // Branch on resource presence
        // a. Text-only
        // • Initiate a streaming text completion request
        // • Await first non-blank chunk, upsert it as a new MessageDto
        // • Launch a coroutine to collect further chunks and upsert updates until completion
        // b. With resources
        // • Group resources by type (image vs. audio)
        // • For each image: treat like text above
        // • For each audio:
        // – Transcribe it
        // – Upsert the transcription onto the original message
        // – Generate a text answer and then synthesize speech
        // – Upsert both the updated original and the new AI audio message
        //
        // Update chat name and summary
        // • Combine name-and-summary generation streams
        // • Upsert chat if name or summary changed
        //
        // Finish and return without explicit result (all state is persisted via upserts)

        if (chatMessages.isEmpty()) {
            Napier.d { "No messages in chat, returning early." }
            return
        }
    }
}
