package illyan.butler.manager

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.repository.ChatRepository
import illyan.butler.repository.MessageRepository
import illyan.butler.util.log.randomUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    private val _userChats = MutableStateFlow<List<DomainChat>>(emptyList())
    val userChats = _userChats.asStateFlow()

    suspend fun loadMoreChat(
        limit: Int = 20,
        timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) {
        val userUUID = authManager.signedInUserUUID.first()
        val newChats = chatRepository.getUserChatsFlow(userUUID!!, limit, timestamp).filterNot { it.second }.map { it.first }.first()
        _userChats.update { (it + newChats.orEmpty()).distinct() }
    }

    fun getChatFlow(uuid: String) = chatRepository.getChatFlow(uuid).map { it.first }
    fun getMessagesByChatFlow(
        uuid: String,
        limit: Int = 20,
        timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) = messageRepository.getChatFlow(uuid, limit, timestamp).map { it.first }

    suspend fun startNewChat(modelUUID: String): String {
        val chatUUID = randomUUID()
        authManager.signedInUserUUID.first()?.let { userUUID ->
            chatRepository.upsert(
                DomainChat(
                    id = chatUUID,
                    members = listOf(userUUID, modelUUID)
                )
            )
        }
        return chatUUID
    }

    suspend fun nameChat(chatUUID: String, name: String) {
        userChats.first().firstOrNull { it.id == chatUUID }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name))
        }
    }

    suspend fun sendMessage(chatUUID: String, message: String) {
        authManager.signedInUserUUID.first()?.let { userUUID ->
            messageRepository.upsert(
                DomainMessage(
                    id = randomUUID(),
                    chatId = chatUUID,
                    role = DomainMessage.USER_ROLE,
                    message = message,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    senderUUID = userUUID
                )
            )
        }
    }
}