package illyan.butler.services.chat.data.exposed

import illyan.butler.services.chat.data.db.ChatDatabase
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
class ChatExposedDatabase(
    private val database: Database
) : ChatDatabase {
}