package illyan.butler.manager

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.repository.ChatRepository
import illyan.butler.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    private val _userChats = MutableStateFlow<List<DomainChat>>(emptyList())
    val userChats = _userChats.asStateFlow()

    init {
        authManager.signedInUserId.map { loadChat() }
    }

    suspend fun loadChat() {
        val userId = authManager.signedInUserId.first()
        if (userId == null) {
            _userChats.update { emptyList() }
            return
        }
        val newChats = chatRepository.getUserChatsFlow(userId).filterNot { it.second }.map { it.first }.first()
        _userChats.update { (it + newChats.orEmpty()).distinct() }
    }

    fun getChatFlow(uuid: String) = chatRepository.getChatFlow(uuid).map { it.first }
    fun getMessagesByChatFlow(uuid: String) = messageRepository.getChatFlow(uuid).map { it.first }

    suspend fun startNewChat(modelUUID: String): String {
        return authManager.signedInUserId.first()?.let { userUUID ->
            chatRepository.upsert(
                DomainChat(
                    members = listOf(userUUID, modelUUID)
                )
            )
        } ?: throw IllegalArgumentException("User not signed in")
    }

    suspend fun nameChat(chatUUID: String, name: String) {
        userChats.first().firstOrNull { it.id == chatUUID }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name))
        }
    }

    suspend fun sendMessage(chatUUID: String, message: String) {
        authManager.signedInUserId.first()?.let { userUUID ->
            messageRepository.upsert(
                DomainMessage(
                    chatId = chatUUID,
                    senderId = userUUID,
                    message = message
                )
            )
        }
    }
}