package illyan.butler.services.chat.data.schema

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import illyan.butler.services.chat.data.utils.NanoIdTable

object Messages : NanoIdTable() {
    val senderId = varchar("senderId", NanoIdUtils.DEFAULT_SIZE)
    val message = text("message").nullable()
    val time = long("time")
    val chatId = entityId("chat", Chats)
}