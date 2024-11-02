package illyan.butler.data.local.model

data class DataHistory(
    val key: String,
    val lastFailedTimestamp: Long,
    val group: String
)
