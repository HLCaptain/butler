package illyan.butler.server.data.schema

import illyan.butler.shared.model.chat.FilterOption
import illyan.butler.shared.model.chat.PromptConfiguration
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.json.json

object Users : UUIDTable() {
    val email = text("email").uniqueIndex()
    val username = text("username").nullable()
    val displayName = text("displayName").nullable()
    val phone = text("phone").nullable()
    val fullName = text("fullName").nullable()
    val photoUrl = text("photoUrl").nullable()
    // Address
    val street = text("street").nullable()
    val city = text("city").nullable()
    val state = text("state").nullable()
    val zip = text("zip").nullable()
    val filters = json("filters", Json, SetSerializer(FilterOption.serializer())) // JSON string for user filters
    val promptConfigurations = json("configurationOptions", Json, ListSerializer(PromptConfiguration.serializer())) // JSON string for custom prompts
}
