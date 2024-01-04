package ai.nest.api_gateway.data.model.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val userId: String? = null,
    val topicId: String? = null, // specific ID related to a topic
    val title: String,
    val body: String,
    val topic: String? = null
)