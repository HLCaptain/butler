package illyan.butler.api_gateway.endpoints.utils

import io.github.aakira.napier.Napier
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
class WebSocketServerHandler {
    private val sessions: ConcurrentHashMap<String, Set<DefaultWebSocketServerSession>> = ConcurrentHashMap()
    private val flows: ConcurrentHashMap<String, Flow<*>> = ConcurrentHashMap()

    private suspend inline fun <reified T> tryToCollect(values: Flow<T>, session: DefaultWebSocketServerSession, crossinline onCloseConnection: () -> Unit = {}) {
        try {
            values.flowOn(Dispatchers.IO).collect { value ->
                Napier.v { "Sending value: $value" }
                session.sendSerialized(value)
            }
        } catch (e: Exception) {
            Napier.e("Error in sending value", e)
            session.close(CloseReason(CloseReason.Codes.NORMAL, e.message.toString()))
            onCloseConnection()
        }
    }

    private fun remove(key: String, session: DefaultWebSocketServerSession) {
        sessions[key] = sessions[key]?.minus(session) ?: emptySet()
    }

    suspend fun addFlowSessionListener(key: String, session: DefaultWebSocketServerSession, defaultFlow: () -> Flow<*>) {
        sessions[key] = sessions[key]?.plus(session) ?: setOf(session)
        flows[key] = defaultFlow()
        tryToCollect(flows[key]!!, session) {
            remove(key, session)
        }
    }
}