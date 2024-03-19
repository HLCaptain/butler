package illyan.butler.services.identity.data.schema

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.jetbrains.exposed.sql.Table

object ChatMembers : Table() {
    val chatId = entityId("chatId", Chats)
    val userId = varchar("userId", NanoIdUtils.DEFAULT_SIZE)
    override val primaryKey = PrimaryKey(chatId, userId)
}