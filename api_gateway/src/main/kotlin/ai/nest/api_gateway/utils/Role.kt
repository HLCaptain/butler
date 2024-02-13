package ai.nest.api_gateway.utils

import kotlinx.serialization.Serializable

@Serializable
data object Role {
    const val END_USER = 1
    const val BOT_OWNER = 2
    const val SUPPORT = 4
    const val ADMIN = 8
}