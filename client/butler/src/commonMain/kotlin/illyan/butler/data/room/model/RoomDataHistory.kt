package illyan.butler.data.room.model

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
