package illyan.butler.server.di

import illyan.butler.server.AppConfig
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): R2dbcDatabase = R2dbcDatabase.connect {
    defaultR2dbcIsolationLevel = IsolationLevel.SERIALIZABLE

    setUrl(AppConfig.Database.DATABASE_URL)

    connectionFactoryOptions {
        option(ConnectionFactoryOptions.USER, AppConfig.Database.DATABASE_USER)
        option(ConnectionFactoryOptions.PASSWORD, AppConfig.Database.DATABASE_PASSWORD)
        option(ConnectionFactoryOptions.DATABASE, AppConfig.Database.DATABASE_NAME)
    }
}
