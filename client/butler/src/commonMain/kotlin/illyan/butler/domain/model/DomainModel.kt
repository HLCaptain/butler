package illyan.butler.domain.model

data class DomainModel(
    val name: String,
    val id: String,
    val type: String,
    val description: String,
    val greetingMessage: String,
    val author: String,
)
