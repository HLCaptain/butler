package illyan.butler.data.ktorfit.datasource

import illyan.butler.data.ktorfit.api.ChatApi
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.ChatDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ChatKtorDataSource(
    private val chatApi: ChatApi
) : ChatNetworkDataSource {
    override fun fetch(uuid: String): Flow<ChatDto> = flow {
        emit(chatApi.fetchChat(uuid))
    }.catch { exception ->
        Napier.e("There was a problem while loading chat", exception)
    }

    override fun fetchByUser(userUUID: String): Flow<List<ChatDto>> = flow {
        emit(chatApi.fetchChatsByUser(userUUID))
    }.catch { exception ->
        Napier.e("There was a problem while loading chats for user", exception)
    }

    override fun fetchByModel(modelUUID: String): Flow<List<ChatDto>> = flow {
        emit(chatApi.fetchChatsByModel(modelUUID))
    }.catch { exception ->
        Napier.e("There was a problem while loading chats for model", exception)
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        return chatApi.upsertChat(chat)
    }

    override suspend fun delete(uuid: String): Boolean {
        return chatApi.deleteChat(uuid)
    }

    override suspend fun deleteForUser(userUUID: String): Boolean {
        return chatApi.deleteChatsForUser(userUUID)
    }
}