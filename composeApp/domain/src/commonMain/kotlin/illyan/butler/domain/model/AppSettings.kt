package illyan.butler.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class AppSettings @OptIn(ExperimentalUuidApi::class) constructor(
    val clientId: String = Uuid.random().toString(),
    val preferences: DomainPreferences = DomainPreferences.Default,
    val hostUrl: String = "",
    val signedInUserId: String? = null
) {
    companion object {
        val Default = AppSettings()
    }
}