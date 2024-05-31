package illyan.butler.api_gateway.endpoints.utils

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import org.koin.core.annotation.Single

@Single
class WebSocketSessionManager(
    private val webSocketClientProvider: () -> HttpClient
) {
    private val sessions = mutableMapOf<String, DefaultClientWebSocketSession>()

    suspend fun createSession(path: String): DefaultClientWebSocketSession {
        val client = webSocketClientProvider()
        val session = client.webSocketSession(
            host = path.substringAfter("://").takeWhile { it != ':' },
            port = path.takeLastWhile { it != ':' }.takeWhile { it != '/' }.toInt(),
            path = path.takeLastWhile { it != ':' }.substringAfter("/")
        )
        Napier.v { "Created websocket session for ${session.call.request.url}" }
        sessions[path]?.close(CloseReason(CloseReason.Codes.NORMAL, "Reconnecting"))
        sessions[path] = session
        return session
    }

    suspend fun closeSession(path: String) {
        sessions[path]?.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
        sessions.remove(path)
    }

    suspend fun closeAllSessions() {
        sessions.forEach { (_, session) ->
            session.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
        }
        sessions.clear()
    }
}