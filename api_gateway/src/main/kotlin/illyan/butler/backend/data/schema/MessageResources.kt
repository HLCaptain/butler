package illyan.butler.backend.data.schema

import org.jetbrains.exposed.sql.Table

object MessageResources : Table() {
    val messageId = entityId("message", Messages)
    val resourceId = entityId("resource", Resources)
    override val primaryKey = PrimaryKey(messageId, resourceId)
}