package illyan.butler.services.chat.di

import illyan.butler.services.chat.AppConfig
import io.github.aakira.napier.Napier
import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.api.BatchOptions
import org.redisson.api.RedissonClient
import org.redisson.config.Config

@Single
fun provideRedisClient(): RedissonClient = Redisson.create(
    Config().apply {
        Napier.d("Providing Redisson client")
        useSingleServer().apply {
            address = AppConfig.Database.REDIS_URL
            username = AppConfig.Database.REDIS_USER
            password = AppConfig.Database.REDIS_PASSWORD
        }
    }
)