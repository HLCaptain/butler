package illyan.butler.backend.data.schema

import illyan.butler.backend.data.utils.NanoIdTable

object Resources : NanoIdTable() {
    val type = text("type") // Content type (MIME types): image/jpeg, audio/mpeg, video/mp4, audio/ogg, etc.
    val data = binary("data")
}