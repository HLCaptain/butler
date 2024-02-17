package illyan.butler.data.ktorfit.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import illyan.butler.data.network.model.ChatDto

interface ChatApi {
    @GET("/chats/{uuid}")
    suspend fun fetchChat(@Path("uuid") uuid: String): ChatDto

    @GET("/chats/user/{userUUID}")
    suspend fun fetchChatsByUser(@Path("userUUID") userUUID: String): List<ChatDto>

    @GET("/chats/model/{modelUUID}")
    suspend fun fetchChatsByModel(@Path("modelUUID") modelUUID: String): List<ChatDto>

    @POST("/chats")
    suspend fun upsertChat(@Body chat: ChatDto)

    @DELETE("/chats/{uuid}")
    suspend fun deleteChat(@Path("uuid") uuid: String)

    @DELETE("/chats/user/{userUUID}")
    suspend fun deleteChatsForUser(@Path("userUUID") userUUID: String)
}