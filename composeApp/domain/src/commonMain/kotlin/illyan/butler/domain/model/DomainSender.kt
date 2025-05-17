package illyan.butler.domain.model

sealed interface DomainSender {
    data class User(
        val userId: String,
    ) : DomainSender

    data class Model(
        val modelConfig: ModelConfig
    ) : DomainSender
}
