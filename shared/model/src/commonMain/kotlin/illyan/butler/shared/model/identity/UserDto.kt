package illyan.butler.shared.model.identity

import illyan.butler.shared.model.chat.FilterOption
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class UserDto(
    val id: Uuid,
    val email: String,
    val username: String? = null,
    val displayName: String? = null,
    val phone: String? = null,
    val fullName: String? = null,
    val photoUrl: String? = null,
    val address: AddressDto? = null,
    val filters: Set<FilterOption>
)
