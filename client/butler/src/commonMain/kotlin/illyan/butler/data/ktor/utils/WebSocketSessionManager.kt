package illyan.butler.data.ktor.utils

import illyan.butler.repository.HostRepository
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single
class WebSocketSessionManager(
    private val webSocketClientProvider: () -> HttpClient,
    private val hostRepository: HostRepository
) {
    private val sessions = mutableMapOf<String, DefaultClientWebSocketSession>()

    suspend fun createSession(path: String): DefaultClientWebSocketSession {
        val client = webSocketClientProvider()
        val currentApiUrl = hostRepository.currentHost.first()
        val realPath = currentApiUrl + path
        // realpath should be like: "protocol://host:port/path"
        val session = client.webSocketSession(
            host = realPath.substringAfter("://").takeWhile { it != ':' },
            port = realPath.takeLastWhile { it != ':' }.takeWhile { it != '/' }.toInt(),
            path = realPath.takeLastWhile { it != ':' }.substringAfter("/")
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