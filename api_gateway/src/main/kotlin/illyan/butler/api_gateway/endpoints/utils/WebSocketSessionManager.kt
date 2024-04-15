package illyan.butler.api_gateway.endpoints.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.close
import org.koin.core.annotation.Single

@Single
class WebSocketSessionManager(
    private val webSocketClientProvider: () -> HttpClient
) {
    private val sessions = mutableMapOf<String, DefaultClientWebSocketSession>()

    suspend fun createSession(url: String): DefaultClientWebSocketSession {
        val client = webSocketClientProvider()
        val session = client.webSocketSession(urlString = url)
        sessions[url]?.close(CloseReason(CloseReason.Codes.NORMAL, "Reconnecting"))
        sessions[url] = session
        return session
    }

    suspend fun closeSession(url: String) {
        sessions[url]?.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
        sessions.remove(url)
    }

    suspend fun closeAllSessions() {
        sessions.forEach { (_, session) ->
            session.close(CloseReason(CloseReason.Codes.NORMAL, "Closed by user"))
        }
        sessions.clear()
    }
}