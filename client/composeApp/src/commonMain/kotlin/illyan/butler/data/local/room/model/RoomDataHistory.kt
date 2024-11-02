package illyan.butler.data.local.room.model

import androidx.room.Entity

@Entity(
    tableName = "data_history",
    primaryKeys = ["key"],
)
data class RoomDataHistory(
    val key: String,
    val lastFailedTimestamp: Long,
    val group: String
)
