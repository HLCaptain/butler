package illyan.butler.api_gateway.endpoints.utils

import illyan.butler.api_gateway.data.service.IdentityService
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single

@Single
class WebSocketServerHandler(
    private val identityService: IdentityService,
) {

    val sessions: ConcurrentHashMap<String, DefaultWebSocketServerSession> = ConcurrentHashMap()

    suspend inline fun <reified T> tryToCollect(values: Flow<T>, session: DefaultWebSocketServerSession) {
        try {
            values.flowOn(Dispatchers.IO).collect { value -> session.sendSerialized(value) }
        } catch (e: Exception) {
            session.close(CloseReason(CloseReason.Codes.NORMAL, e.message.toString()))
        }
    }
}