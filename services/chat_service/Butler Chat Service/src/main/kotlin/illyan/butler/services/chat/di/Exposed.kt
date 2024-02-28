package illyan.butler.services.chat.di

import illyan.butler.services.chat.AppConfig
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): Database = Database.connect(
    url = AppConfig.Exposed.DATABASE_URL,
    driver = AppConfig.Exposed.DATABASE_DRIVER,
    user = AppConfig.Exposed.DATABASE_USER,
    password = AppConfig.Exposed.DATABASE_PASSWORD
)
