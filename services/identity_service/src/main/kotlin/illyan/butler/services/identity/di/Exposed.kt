package illyan.butler.services.identity.di

import illyan.butler.services.identity.AppConfig
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
        url = "${AppConfig.Database.DATABASE_URL}/${AppConfig.Database.DATABASE_NAME}",
        driver = AppConfig.Database.DATABASE_DRIVER,
        user = AppConfig.Database.DATABASE_USER,
        password = AppConfig.Database.DATABASE_PASSWORD
    )
}
