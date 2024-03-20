package illyan.butler.services.identity.data.schema

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.jetbrains.exposed.sql.Table

object UserPasswords : Table() {
    val userId = varchar("userId", NanoIdUtils.DEFAULT_SIZE)
    val passwordHash = text("passwordHash")
    override val primaryKey = PrimaryKey(userId)
}