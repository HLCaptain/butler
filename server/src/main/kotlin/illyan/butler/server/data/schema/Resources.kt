package illyan.butler.server.data.schema

import org.jetbrains.exposed.dao.id.UUIDTable

object Resources : UUIDTable() {
    val type = text("type") // Content type (MIME types): image/jpeg, audio/mpeg, video/mp4, audio/ogg, etc.
    val data = binary("data")
}