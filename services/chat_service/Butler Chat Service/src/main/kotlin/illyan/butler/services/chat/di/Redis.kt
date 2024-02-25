package illyan.butler.services.chat.di

import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.config.Config

@Single
fun provideRedisClient() = Redisson.create(
    Config().apply {
        useSingleServer().address = "redis://localhost:6379"
    }
)