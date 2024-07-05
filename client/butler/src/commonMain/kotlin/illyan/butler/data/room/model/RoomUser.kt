package illyan.butler.data.room.model

import androidx.room.Entity
import illyan.butler.domain.model.DomainAddress
import illyan.butler.domain.model.DomainToken

@Entity(tableName = "users", primaryKeys = ["id"])
data class RoomUser(
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
