package illyan.butler.server.data.schema

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object Users : UUIDTable() {
    val email = text("email").uniqueIndex()
    val username = text("username").nullable()
    val displayName = text("displayName").nullable()
    val phone = text("phone").nullable()
    val fullName = text("fullName").nullable()
    val photoUrl = text("photoUrl").nullable()
    // Address
    val street = text("street").nullable()
    val city = text("city").nullable()
    val state = text("state").nullable()
    val zip = text("zip").nullable()
}
