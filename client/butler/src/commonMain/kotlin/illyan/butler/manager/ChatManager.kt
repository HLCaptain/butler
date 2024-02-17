package illyan.butler.manager

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.repository.ChatRepository
import illyan.butler.repository.MessageRepository
import illyan.butler.util.log.randomUUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val userChats = authManager.signedInUserUUID.flatMapLatest { uuid ->
        uuid?.let {
            chatRepository.getUserChatsFlow(it)
        }?.filterNot { it.second }?.map { it.first } ?: flowOf(emptyList())
    }
    val userChatsPerModel = userChats.map { chats ->
        chats?.groupBy { it.modelUUID }
    }

    fun getChatFlow(uuid: String) = chatRepository.getChatFlow(uuid).map { it.first }
    fun getMessagesByChatFlow(uuid: String) = messageRepository.getChatFlow(uuid).map { it.first }

    suspend fun startNewChat(modelUUID: String): String {
        val chatUUID = randomUUID()
        authManager.signedInUserUUID.first()?.let { userUUID ->
            chatRepository.upsert(
                DomainChat(
                    uuid = chatUUID,
                    userUUID = userUUID,
                    modelUUID = modelUUID
                )
            )
        }
        return chatUUID
    }

    suspend fun nameChat(chatUUID: String, name: String) {
        userChats.first()?.firstOrNull { it.uuid == chatUUID }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name))
        }
    }

    suspend fun sendMessage(chatUUID: String, message: String) {
        authManager.signedInUserUUID.first()?.let { userUUID ->
            messageRepository.upsert(
                DomainMessage(
                    uuid = randomUUID(),
                    chatUUID = chatUUID,
                    role = DomainMessage.USER_ROLE,
                    message = message,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    senderUUID = userUUID
                )
            )
        }
    }
}