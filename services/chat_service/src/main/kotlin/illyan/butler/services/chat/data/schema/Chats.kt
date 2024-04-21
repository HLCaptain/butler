package illyan.butler.services.chat.data.schema

import illyan.butler.services.chat.data.utils.NanoIdTable

object Chats : NanoIdTable() {
    val name = text("name").nullable()
    val created = long("created")
}







