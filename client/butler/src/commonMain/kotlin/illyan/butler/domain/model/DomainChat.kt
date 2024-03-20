package illyan.butler.domain.model

data class DomainChat(
    val id: String,
    val name: String? = null,
    val members: List<String>
)

