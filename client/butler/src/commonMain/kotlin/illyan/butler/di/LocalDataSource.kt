package illyan.butler.di

import illyan.butler.config.BuildConfig
import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.local.datasource.ResourceLocalDataSource
import illyan.butler.data.room.datasource.ChatRoomDataSource
import illyan.butler.data.room.datasource.DataHistoryRoomDataSource
import illyan.butler.data.room.datasource.MessageRoomDataSource
import illyan.butler.data.room.datasource.ResourceRoomDataSource
import illyan.butler.data.sqldelight.datasource.ChatSqlDelightDataSource
import illyan.butler.data.sqldelight.datasource.DataHistorySqlDelightDataSource
import illyan.butler.data.sqldelight.datasource.MessageSqlDelightDataSource
import illyan.butler.data.sqldelight.datasource.ResourceSqlDelightDataSource
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

@Single
fun provideChatLocalDataSource(
    sqlDelightDataSource: ChatSqlDelightDataSource,
    roomDataSource: ChatRoomDataSource
): ChatLocalDataSource {
    return if (BuildConfig.USE_ROOM_DB) {
        roomDataSource
    } else {
        sqlDelightDataSource
    }
}

@Single
fun provideResourceLocalDataSource(
    sqlDelightDataSource: ResourceSqlDelightDataSource,
    roomDataSource: ResourceRoomDataSource
): ResourceLocalDataSource {
    return if (BuildConfig.USE_ROOM_DB) {
        roomDataSource
    } else {
        sqlDelightDataSource
    }
}
