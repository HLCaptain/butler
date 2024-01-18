package ai.nest.api_gateway.utils

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    END_USER,
    BOT_OWNER,
    ADMIN,
    SUPPORT
}