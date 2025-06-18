package illyan.butler.server.di

import com.aallam.openai.client.OpenAI
import illyan.butler.server.data.service.ChatService
import illyan.butler.shared.llm.LlmService
import illyan.butler.shared.model.chat.MessageDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
fun provideLlmService(
    chatService: ChatService,
    coroutineScope: CoroutineScope,
    @Named("OpenAIClients") openAIClients: Map<String, OpenAI>
) = LlmService(
    coroutineScopeIO = coroutineScope + CoroutineExceptionHandler { _, t ->
        Napier.e("Error while processing chat", t)
    },
    getResource = { resourceId, senderId ->
        chatService.getResource(
            resourceId, senderId
        )
    },
    createResource = { chatId, modelId, resource ->
        val newResource = chatService.createResource(
            modelId,
            resource
        )
        chatService.sendMessage(
            modelId,
            MessageDto(
                id = null,
                senderId = modelId,
                chatId = chatId,
                resourceIds = listOf(newResource.id!!)
            )
        )
        newResource
    },
    upsertMessage = { message ->
        chatService.editMessage(
            message.senderId,
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
    errorInMessageResponse = { message ->
        message?.let {
            chatService.deleteMessage(
                it.senderId,
                it.chatId,
                it.id!!
            )
        }
        Napier.e { "Error in message response" }
    },
    removeMessage = { message ->
        chatService.deleteMessage(
            message.senderId,
            message.chatId,
            message.id!!
        )
    }
)