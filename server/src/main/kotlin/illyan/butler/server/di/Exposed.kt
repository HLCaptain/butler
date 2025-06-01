package illyan.butler.server.di

import illyan.butler.server.AppConfig
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): R2dbcDatabase = R2dbcDatabase.connect {
    defaultR2dbcIsolationLevel = IsolationLevel.SERIALIZABLE

    connectionFactoryOptions {
        option(ConnectionFactoryOptions.USER, AppConfig.Database.USER)
        option(ConnectionFactoryOptions.DRIVER, AppConfig.Database.DRIVER)
        option(ConnectionFactoryOptions.PASSWORD, AppConfig.Database.PASSWORD)
        option(ConnectionFactoryOptions.DATABASE, AppConfig.Database.NAME)
        option(ConnectionFactoryOptions.HOST, AppConfig.Database.HOST)
        option(ConnectionFactoryOptions.PORT, AppConfig.Database.PORT)
        option(ConnectionFactoryOptions.PROTOCOL, "r2dbc")
    }
}
