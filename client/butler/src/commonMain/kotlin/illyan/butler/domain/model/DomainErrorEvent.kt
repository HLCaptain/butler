package illyan.butler.domain.model

data class DomainErrorEvent(
    val id: String,
    val platform: String,
    val exception: String,
    val message: String,
    val stackTrace: String,
    val metadata: Map<String, String>,
    val os: String,
    val timestamp: Long,
    val state: ErrorState
)
