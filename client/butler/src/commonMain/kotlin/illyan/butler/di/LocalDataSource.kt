package illyan.butler.di

import illyan.butler.config.BuildConfig
import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.room.datasource.DataHistoryRoomDataSource
import illyan.butler.data.room.datasource.MessageRoomDataSource
import illyan.butler.data.sqldelight.datasource.DataHistorySqlDelightDataSource
import illyan.butler.data.sqldelight.datasource.MessageSqlDelightDataSource
import org.koin.core.annotation.Single

@Single
fun provideDataHistoryLocalDataSource(
    sqlDelightDataSource: DataHistorySqlDelightDataSource,
    roomDataSource: DataHistoryRoomDataSource
): DataHistoryLocalDataSource {
    return if (BuildConfig.USE_ROOM_DB) {
        roomDataSource
    } else {
        sqlDelightDataSource
    }
}

@Single
fun provideMessageLocalDataSource(
    sqlDelightDataSource: MessageSqlDelightDataSource,
    roomDataSource: MessageRoomDataSource
): MessageLocalDataSource {
    return if (BuildConfig.USE_ROOM_DB) {
        roomDataSource
    } else {
        sqlDelightDataSource
    }
}