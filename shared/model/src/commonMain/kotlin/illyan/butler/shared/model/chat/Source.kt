package illyan.butler.shared.model.chat

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface Source {
    data class Device(
        val deviceId: Uuid
    ) : Source

    data class Server(
        val userId: Uuid,
        val endpoint: String, // e.g. "https://api.example.com/v1/"
    ) : Source
}
