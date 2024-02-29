package illyan.butler.data.ktorfit.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import illyan.butler.data.network.model.MessageDto

interface MessageApi {
    @GET("/messages/{uuid}")
    suspend fun fetchMessage(@Path("uuid") uuid: String): MessageDto

    @GET("/messages/chat/{chatUUID}")
    suspend fun fetchMessagesByChat(@Path("chatUUID") chatUUID: String): List<MessageDto>

    @POST("/messages")
    suspend fun upsertMessage(@Body message: MessageDto): MessageDto

    @DELETE("/messages/{uuid}")
    suspend fun deleteMessage(@Path("uuid") uuid: String): Boolean

    @DELETE("/messages/chat/{chatUUID}")
    suspend fun deleteMessagesForChat(@Path("chatUUID") chatUUID: String): Boolean
}