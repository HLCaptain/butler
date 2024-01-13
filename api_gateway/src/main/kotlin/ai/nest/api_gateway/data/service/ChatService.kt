package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.chat.MessageDto
import ai.nest.api_gateway.data.model.chat.TicketDto
import ai.nest.api_gateway.data.utils.ErrorHandler
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.data.utils.tryToExecuteWebSocket
import ai.nest.api_gateway.data.utils.tryToSendAndReceiveWebSocketData
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.util.Attributes
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@Single
class ChatService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val errorHandler: ErrorHandler
) {
    @OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
    suspend fun createTicket(
        ticket: TicketDto,
        languageCode: String
    ) = client.tryToExecute<TicketDto>(
        api = APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        post("/chat/ticket") {
            body = ProtoBuf.encodeToByteArray(TicketDto.serializer(), ticket)
        }
    }

    suspend fun updateTicketState(
        ticketId: String,
        state: Boolean,
        languageCode: String
    ) = client.tryToExecute<Boolean>(
        api = APIs.CHAT_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        put("/chat/$ticketId") {
            parameter("state", state)
        }
    }

    suspend fun receiveTicket(supportId: String) = client.tryToExecuteWebSocket<TicketDto>(
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/tickets/$supportId"
    )

    suspend fun sendAndReceiveMessage(
        message: MessageDto,
        chatId: String
    ) = client.tryToSendAndReceiveWebSocketData(
        data = message,
        api = APIs.CHAT_API,
        attributes = attributes,
        path = "/chat/$chatId"
    )
}
