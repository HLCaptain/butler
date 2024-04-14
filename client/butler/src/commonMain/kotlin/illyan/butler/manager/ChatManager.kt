package illyan.butler.manager

import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.repository.ChatRepository
import illyan.butler.repository.MessageRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) {
    private val _userChats = MutableStateFlow<List<DomainChat>>(emptyList())
    val userChats = _userChats.asStateFlow()

    init {
        coroutineScopeIO.launch {
            authManager.signedInUserId.flatMapLatest {
                _userChats.update { emptyList() }
                loadChats(it)
            }.collectLatest { newChats ->
                _userChats.update { (it + newChats).distinct() }
            }
        }
    }

    private fun loadChats(userId: String? = authManager.signedInUserId.value): Flow<List<DomainChat>> {
        if (userId == null) {
            Napier.v { "User not signed in, reseting chats" }
            return flowOf(emptyList())
        }
        Napier.v { "Loading chats for user $userId" }
        return chatRepository.getUserChatsFlow(userId).filterNot { it.second }.map { it.first ?: emptyList() }
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