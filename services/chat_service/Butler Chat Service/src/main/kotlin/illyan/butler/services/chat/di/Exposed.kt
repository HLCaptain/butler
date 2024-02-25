package illyan.butler.services.chat.di

import org.jetbrains.exposed.sql.Database
import org.koin.core.annotation.Single

@Single
fun provideDatabase(): Database = Database.connect(
    url = "jdbc:postgresql://localhost:12346/test",
    driver = "org.postgresql.Driver",
    user = "root",
    password = "your_pwd"
)