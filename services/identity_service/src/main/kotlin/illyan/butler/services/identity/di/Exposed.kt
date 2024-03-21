package illyan.butler.services.identity.di

import illyan.butler.services.identity.AppConfig
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): Database = Database.connect(
    url = "${AppConfig.Database.DATABASE_URL}/${AppConfig.Database.DATABASE_NAME}",
    driver = AppConfig.Database.DATABASE_DRIVER,
    user = AppConfig.Database.DATABASE_USER,
    password = AppConfig.Database.DATABASE_PASSWORD
)
