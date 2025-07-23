package illyan.butler.server.di

import com.aallam.openai.client.OpenAI
import illyan.butler.server.data.service.ChatService
import illyan.butler.shared.llm.createLlmService
import illyan.butler.shared.model.chat.MessageDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
fun provideLlmService(
    chatService: ChatService,
    coroutineScope: CoroutineScope,
    @Named("OpenAIClients") openAIClients: Map<String, OpenAI>
) = createLlmService(
    coroutineScopeIO = coroutineScope + CoroutineExceptionHandler { _, t ->
        Napier.e("Error while processing chat", t)
    },
    getResource = { resourceId, senderId ->
        chatService.getResource(
            resourceId,
            senderId
        )
    },
    createResource = { userId, chatId, modelId, resource ->
        val newResource = chatService.createResource(resource)
        chatService.sendMessage(
            userId,
            MessageDto(
                id = Uuid.random(),
                sender = modelId,
                chatId = chatId,
                resourceIds = listOf(newResource.id)
            )
        )
        newResource
    },
    upsertMessage = { userId, message ->
        chatService.editMessage(
            Uuid.parse(message.senderId), // Assuming senderId is a Uuid string (Sender is eaither AI or User, NOT SYSTEM)
            message
        )
    },
    getOpenAIClient = { endpoint ->
        openAIClients[endpoint] ?: error("No OpenAI client found for endpoint $endpoint")
    },
    upsertChat = { chat ->
        chatService.editChat(
            chat.ownerId,
            chat
        )
    },
    errorInMessageResponse = { userId, message ->
        message?.let {
            chatService.deleteMessage(
                userId,
                it.chatId,
                it.id
            )
        }
        Napier.e { "Error in message response" }
    },
    removeMessage = { userId, message ->
        chatService.deleteMessage(
            userId,
            message.chatId,
            message.id
        )
    }
)