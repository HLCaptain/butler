package illyan.butler.server.data.schema

import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.json.json

object Chats : UUIDTable() {
    val name = text("name").nullable()
    val created = long("createdAt")
    val models = json<Map<Capability, AiSource>>("models", Json { allowStructuredMapKeys = true })
    val summary = text("summary").nullable()
    val ownerId = reference("ownerId", Users)
}
