package ai.nest.api_gateway.endpoints.utils

import io.ktor.server.websocket.DefaultWebSocketServerSession
import java.util.concurrent.ConcurrentHashMap
import org.koin.core.annotation.Single

class Connection(val session: DefaultWebSocketServerSession)

@Single
class ChatSocketHandler {
    val connections: ConcurrentHashMap<String, LinkedHashSet<Connection>> = ConcurrentHashMap()
}