package illyan.butler.services.identity.data.schema

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.jetbrains.exposed.sql.Table

object UserPasswords : Table() {
    val userId = varchar("userId", NanoIdUtils.DEFAULT_SIZE)
    val hash = text("hash")
    override val primaryKey = PrimaryKey(userId)
}