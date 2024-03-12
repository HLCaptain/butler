package illyan.butler.domain.model

data class DomainChat(
    val uuid: String,
    val name: String? = null,
    val members: List<String>
)

