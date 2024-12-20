package illyan.butler.server.di

import illyan.butler.server.AppConfig
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): Database = Database.connect(
    url = "${AppConfig.Database.DATABASE_URL}/${AppConfig.Database.DATABASE_NAME}",
    driver = AppConfig.Database.DATABASE_DRIVER,
    user = AppConfig.Database.DATABASE_USER,
    password = AppConfig.Database.DATABASE_PASSWORD
)
