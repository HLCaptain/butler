package illyan.butler.services.chat.endpoints.utils

import io.github.aakira.napier.Napier
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class WebSocketServerHandler {
    private val sessions: ConcurrentHashMap<String, Set<DefaultWebSocketServerSession>> = ConcurrentHashMap()
    private val flows: ConcurrentHashMap<String, Flow<*>> = ConcurrentHashMap()

    private suspend inline fun beginFlowCollection(sessionsKey: String) {
        try {
            flows[sessionsKey]?.flowOn(Dispatchers.IO)?.collect { value ->
                Napier.v { "Sending value: $value" }
                sessions[sessionsKey]?.forEach { it.sendSerialized(value) }
            }
        } catch (e: Exception) {
            Napier.e("Error in sending value", e)
        } finally {
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
        sessions[key] = sessions[key]?.plus(session) ?: setOf(session)
        if (!flows.containsKey(key)) {
            flows[key] = defaultFlow()
            beginFlowCollection(key)
        }
    }
}