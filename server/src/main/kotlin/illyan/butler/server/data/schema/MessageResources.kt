package illyan.butler.server.data.schema

import org.jetbrains.exposed.v1.core.Table

object MessageResources : Table() {
    val messageId = reference("message", Messages)
    val resourceId = reference("resource", Resources)
    override val primaryKey = PrimaryKey(messageId, resourceId)
}
