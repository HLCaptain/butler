package illyan.butler.services.chat.data.schema

import illyan.butler.services.chat.data.utils.NanoIdTable

object Resources : NanoIdTable() {
    val type = text("type").uniqueIndex() // Content type (MIME types): image/jpeg, audio/mpeg, video/mp4, audio/ogg, etc.
    val data = binary("data")
}