package illyan.butler.domain.model

data class DomainUser(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: DomainAddress?,
    val accessToken: DomainToken?,
    val refreshToken: DomainToken?
)
