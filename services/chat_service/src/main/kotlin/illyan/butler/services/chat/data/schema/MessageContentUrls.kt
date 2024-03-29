package illyan.butler.services.chat.data.schema

import org.jetbrains.exposed.sql.Table

object MessageContentUrls : Table() {
    val messageId = entityId("message", Messages)
    val urlId = entityId("url", ContentUrls)
    override val primaryKey = PrimaryKey(messageId, urlId)
}