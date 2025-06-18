package illyan.butler.server.data.schema

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object Messages : UUIDTable() {
    val senderId = text("senderId")
    val message = text("message").nullable()
    val time = long("time")
    val chatId = reference("chat", Chats)
}
