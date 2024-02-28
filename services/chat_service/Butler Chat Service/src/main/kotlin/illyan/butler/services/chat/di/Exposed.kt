package illyan.butler.services.chat.di

import illyan.butler.services.chat.AppConfig
import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): Database = if (AppConfig.Ktor.DEVELOPMENT) {
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    )
} else {
    Database.connect(
        url = AppConfig.Exposed.DATABASE_URL,
        driver = AppConfig.Exposed.DATABASE_DRIVER,
        user = AppConfig.Exposed.DATABASE_USER,
        password = AppConfig.Exposed.DATABASE_PASSWORD
    )
}
