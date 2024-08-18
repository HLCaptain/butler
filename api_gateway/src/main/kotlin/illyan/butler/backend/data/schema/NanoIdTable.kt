package illyan.butler.backend.data.schema

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class NanoIdTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, 21).clientDefault { NanoIdUtils.randomNanoId() }.entityId()
    final override val primaryKey = PrimaryKey(id)
}