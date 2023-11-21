package illyan.butler.manager

import illyan.butler.domain.model.DomainChat
import illyan.butler.repository.ChatRepository
import illyan.butler.util.log.randomUUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ChatManager(
    private val authManager: AuthManager,
    private val chatRepository: ChatRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val userChats = authManager.signedInUser.flatMapLatest { user ->
        user?.uid?.let {
            chatRepository.getUserChatsFlow(it)
        }?.filterNot { it.second }?.map { it.first } ?: flowOf(emptyList())
    }
    val userChatsPerModel = userChats.map { chats ->
        chats?.groupBy { it.modelUUID }
    }

    suspend fun startNewChat(modelUUID: String) {
        authManager.signedInUser.map { it?.uid }.first()?.let { userUUID ->
            chatRepository.upsert(
                DomainChat(
                    uuid = randomUUID(),
                    userUUID = userUUID,
                    modelUUID = modelUUID,
                    messages = emptyList()
                )
            )
        }
    }

    suspend fun nameChat(chatUUID: String, name: String) {
        userChats.first()?.firstOrNull { it.uuid == chatUUID }?.let { chat ->
            chatRepository.upsert(chat.copy(name = name))
        }
    }
}