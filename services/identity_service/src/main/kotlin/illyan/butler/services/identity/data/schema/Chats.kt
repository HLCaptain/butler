package illyan.butler.services.identity.data.schema

import illyan.butler.services.identity.data.utils.NanoIdTable

object Chats : NanoIdTable() {
    val name = text("name").nullable()
}







