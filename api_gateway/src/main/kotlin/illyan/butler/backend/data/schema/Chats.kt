package illyan.butler.backend.data.schema

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.json.json

object Chats : NanoIdTable() {
    val name = text("name").nullable()
    val created = long("created")
    val endpoints = json<Map<String, String>>("endpoints", Json.Default)
    val summary = text("summary").nullable()
}
