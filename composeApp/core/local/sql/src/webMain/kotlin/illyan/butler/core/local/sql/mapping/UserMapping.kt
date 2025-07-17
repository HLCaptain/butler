package illyan.butler.core.local.sqldelight.mapping

import illyan.butler.core.local.sqldelight.Users
import illyan.butler.domain.model.User
import illyan.butler.shared.model.chat.FilterOption
import illyan.butler.shared.model.chat.PromptConfiguration
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun User.toSqlDelightModel(): SqlDelightUser {
    return SqlDelightUser(
        id = id.toString(),
        endpoint = endpoint,
        email = email,
        username = username,
        displayName = displayName,
        phone = phone,
        fullName = fullName,
        photoUrl = photoUrl,
        address = address?.let { Json.encodeToString(it) },
        filterOptions = Json.encodeToString(filters),
        customPrompts = Json.encodeToString(promptConfigurations)
    )
}

data class SqlDelightUser(
    val id: String,
    val endpoint: String,
    val email: String,
    val username: String?,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: String?,
    val filterOptions: String,
    val customPrompts: String
)

// Extension function to convert from generated SQLDelight model to domain model
// This will be used once the SQLDelight code is generated
@OptIn(ExperimentalUuidApi::class)
fun Users.toDomainModel(): User {
    return User(
        id = Uuid.parse(id),
        endpoint = endpoint,
        email = email,
        username = username,
        displayName = displayName,
        phone = phone,
        fullName = fullName,
        photoUrl = photoUrl,
        address = address?.let { Json.decodeFromString(it) },
        filters = Json.decodeFromString<Set<FilterOption>>(filterOptions),
        promptConfigurations = Json.decodeFromString<List<PromptConfiguration>>(customPrompts)
    )
}
