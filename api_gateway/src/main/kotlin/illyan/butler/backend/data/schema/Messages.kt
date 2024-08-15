package illyan.butler.backend.data.schema

import illyan.butler.backend.data.utils.NanoIdTable

object Messages : NanoIdTable() {
    val senderId = text("senderId")
    val message = text("message").nullable()
    val time = long("time")
    val chatId = entityId("chat", Chats)
}