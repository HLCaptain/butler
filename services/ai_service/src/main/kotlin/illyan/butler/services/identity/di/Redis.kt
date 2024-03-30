package illyan.butler.services.identity.di

import illyan.butler.services.identity.AppConfig
import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.config.Config

@Single
fun provideRedisClient() = Redisson.create(
    Config().apply {
        useSingleServer().apply {
            address = AppConfig.Database.REDIS_URL
            username = AppConfig.Database.REDIS_USER
            password = AppConfig.Database.REDIS_PASSWORD
        }
    }
)