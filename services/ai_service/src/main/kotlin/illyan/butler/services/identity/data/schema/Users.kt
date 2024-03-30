package illyan.butler.services.identity.data.schema

import illyan.butler.services.identity.data.utils.NanoIdTable

object Users : NanoIdTable() {
    val email = text("email").uniqueIndex()
    val username = text("username").uniqueIndex()
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