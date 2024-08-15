package illyan.butler.backend.data.schema

import org.jetbrains.exposed.sql.Table

object ChatMembers : Table() {
    val chatId = entityId("chatId", Chats)
    val memberId = text("memberId") // Member ID length is not defined (like user ID with length 21)
    override val primaryKey = PrimaryKey(chatId, memberId)
}
