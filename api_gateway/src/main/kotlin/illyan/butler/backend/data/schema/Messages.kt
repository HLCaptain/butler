package illyan.butler.backend.data.schema

object Messages : NanoIdTable() {
    val senderId = text("senderId")
    val message = text("message").nullable()
    val time = long("time")
    val chatId = entityId("chat", Chats)
}