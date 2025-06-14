package illyan.butler.server.data.schema

import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.ModelConfig
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.json.json

object Chats : UUIDTable() {
    val name = text("name").nullable()
    val created = long("createdAt")
    val models = json<Map<Capability, ModelConfig>>("models", Json.Default)
    val summary = text("summary").nullable()
    val ownerId = reference("ownerId", Users)
}
