package illyan.butler.server.data.schema

import org.jetbrains.exposed.v1.core.Table

object UserPasswords : Table() {
    val userId = reference("userId", Users.id)
    val passwordHash = text("passwordHash")
    override val primaryKey = PrimaryKey(userId)
}
