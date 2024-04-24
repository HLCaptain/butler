package illyan.butler.domain.model

data class DomainChat(
    val id: String? = null,
    val created: Long? = null,
    val name: String? = null,
    val members: List<String>,
    val aiEndpoints: Map<String, String>
)

