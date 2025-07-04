package illyan.butler.core.local.sqldelight

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.local.sqldelight.datasource.ChatSqlDelightDataSource
import illyan.butler.core.local.sqldelight.datasource.CredentialSqlDelightDataSource
import illyan.butler.core.local.sqldelight.datasource.DataHistorySqlDelightDataSource
import illyan.butler.core.local.sqldelight.datasource.MessageSqlDelightDataSource
import illyan.butler.core.local.sqldelight.datasource.ResourceSqlDelightDataSource
import illyan.butler.core.local.sqldelight.datasource.UserSqlDelightDataSource
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("illyan.butler.core.local.sqldelight")
class SqlDelightCoreModule {

    @Single
    fun provideButlerDatabase(): ButlerDatabase = createDatabase()

    @Single
    fun provideUserLocalDataSource(database: ButlerDatabase): UserLocalDataSource =
        UserSqlDelightDataSource(database)

    @Single
    fun provideChatLocalDataSource(database: ButlerDatabase): ChatLocalDataSource =
        ChatSqlDelightDataSource(database)

    @Single
    fun provideMessageLocalDataSource(database: ButlerDatabase): MessageLocalDataSource =
        MessageSqlDelightDataSource(database)

    @Single
    fun provideResourceLocalDataSource(database: ButlerDatabase): ResourceLocalDataSource =
        ResourceSqlDelightDataSource(database)

    @Single
    fun provideCredentialLocalDataSource(database: ButlerDatabase): CredentialLocalDataSource =
        CredentialSqlDelightDataSource(database)

    @Single
    fun provideDataHistoryLocalDataSource(database: ButlerDatabase): DataHistoryLocalDataSource =
        DataHistorySqlDelightDataSource(database)
}
