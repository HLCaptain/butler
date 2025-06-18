package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
sealed interface Source {
    @Serializable
    data class Device(
        val deviceId: Uuid
    ) : Source

    @Serializable
    data class Server(
        val userId: Uuid,
        val endpoint: String, // e.g. "https://api.example.com/v1/"
    ) : Source
}
