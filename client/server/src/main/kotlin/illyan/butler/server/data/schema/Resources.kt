package illyan.butler.server.data.schema

object Resources : NanoIdTable() {
    val type = text("type") // Content type (MIME types): image/jpeg, audio/mpeg, video/mp4, audio/ogg, etc.
    val data = binary("data")
}