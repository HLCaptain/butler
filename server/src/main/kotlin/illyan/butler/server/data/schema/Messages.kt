package illyan.butler.server.data.schema

import illyan.butler.shared.model.chat.SenderType
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.json.json

object Messages : UUIDTable() {
    val sender = json("sender", Json, SenderType.serializer())
    val message = text("message").nullable()
    val time = long("time")
    val chatId = reference("chat", Chats)
}
