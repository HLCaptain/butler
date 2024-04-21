package illyan.butler.api_gateway.endpoints.utils

import io.github.aakira.napier.Napier
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class WebSocketServerHandler {
    private val sessions: ConcurrentHashMap<String, Set<DefaultWebSocketServerSession>> = ConcurrentHashMap()
    private val flows: ConcurrentHashMap<String, Flow<*>> = ConcurrentHashMap()

    private suspend inline fun beginFlowCollection(sessionsKey: String) {
        try {
            Napier.v { "Beginning flow collection for $sessionsKey" }
            val closed = mutableSetOf<DefaultWebSocketServerSession>()
            flows[sessionsKey]?.collect { value ->
                Napier.v { "Sending value: $value" }
                sessions[sessionsKey]?.forEach { session ->
                    try {
                        session.sendSerialized(value)
                    } catch (e: Exception) {
                        closed += session
                        Napier.e("Error in sending value for session, ${sessions[sessionsKey]?.filter { it !in closed }?.size} remained in $sessionsKey", e)
                        session.close(CloseReason(CloseReason.Codes.NORMAL, "Error in sending value"))
                    }
                }
                sessions[sessionsKey] = sessions[sessionsKey]?.filter { it !in closed }?.toSet() ?: emptySet()
                if (closed.isNotEmpty()) {
                    Napier.v { "Closed sessions: $closed" }
                    closed.clear()
                }
            }
        } catch (e: Exception) {
            Napier.e("Error in sending value", e)
        } finally {
            Napier.v { "Flow collection ended for $sessionsKey, closing all sessions" }
            sessions[sessionsKey]?.forEach {
                it.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
                remove(sessionsKey, it)
            }
        }
    }

    private fun remove(key: String, session: DefaultWebSocketServerSession) {
        sessions[key] = sessions[key]?.minus(session) ?: emptySet()
        if (sessions[key].isNullOrEmpty()) flows.remove(key)
    }

    suspend fun addFlowSessionListener(key: String, session: DefaultWebSocketServerSession, defaultFlow: () -> Flow<*>) {
        Napier.v { "Adding flow session listener for $key" }
        sessions[key] = sessions[key]?.plus(session) ?: setOf(session)
        if (!flows.containsKey(key)) {
            flows[key] = defaultFlow()
            beginFlowCollection(key)
        }
    }
}
