package illyan.butler.backend.utils

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    END_USER,
    ADMIN
}