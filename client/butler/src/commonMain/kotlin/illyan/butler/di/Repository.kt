package illyan.butler.di

import illyan.butler.config.BuildConfig
import illyan.butler.repository.AppMemoryRepository
import illyan.butler.repository.AppRepository
import illyan.butler.repository.AppSettingsRepository
import illyan.butler.repository.ChatMemoryRepository
import illyan.butler.repository.ChatRepository
import illyan.butler.repository.ChatStoreRepository
import illyan.butler.repository.ErrorLocalRepository
import illyan.butler.repository.ErrorMemoryRepository
import illyan.butler.repository.ErrorRepository
import illyan.butler.repository.HostMemoryRepository
import illyan.butler.repository.HostRepository
import illyan.butler.repository.HostSettingsRepository
import illyan.butler.repository.MessageMemoryRepository
import illyan.butler.repository.MessageRepository
import illyan.butler.repository.MessageStoreRepository
import illyan.butler.repository.ModelMemoryRepository
import illyan.butler.repository.ModelNetworkRepository
import illyan.butler.repository.ModelRepository
import illyan.butler.repository.UserMemoryRepository
import illyan.butler.repository.UserRepository
import illyan.butler.repository.UserSettingsRepository
import org.koin.core.annotation.Single

@Single
fun provideAppRepository(
    appMemoryRepository: AppMemoryRepository,
    appSettingsRepository: AppSettingsRepository
): AppRepository = if (BuildConfig.DEBUG) {
    appMemoryRepository
} else {
    appSettingsRepository
}

@Single
fun provideChatRepository(
    chatMemoryRepository: ChatMemoryRepository,
    chatStoreRepository: ChatStoreRepository
): ChatRepository = if (BuildConfig.USE_MEMORY_DB) {
    chatMemoryRepository
} else {
    chatStoreRepository
}

@Single
fun provideMessageRepository(
    messageMemoryRepository: MessageMemoryRepository,
    messageStoreRepository: MessageStoreRepository
): MessageRepository = if (BuildConfig.USE_MEMORY_DB) {
    messageMemoryRepository
} else {
    messageStoreRepository
}

@Single
fun provideModelRepository(
    modelMemoryRepository: ModelMemoryRepository,
    modelNetworkRepository: ModelNetworkRepository
): ModelRepository = if (BuildConfig.USE_MEMORY_DB) {
    modelMemoryRepository
} else {
    modelNetworkRepository
}

@Single
fun provideHostRepository(
    hostMemoryRepository: HostMemoryRepository,
    hostSettingsRepository: HostSettingsRepository
): HostRepository = if (BuildConfig.USE_MEMORY_DB) {
    hostMemoryRepository
} else {
    hostSettingsRepository
}

@Single
fun provideErrorRepository(
    errorMemoryRepository: ErrorMemoryRepository,
    errorLocalRepository: ErrorLocalRepository
): ErrorRepository = if (BuildConfig.USE_MEMORY_DB) {
    errorMemoryRepository
} else {
    errorLocalRepository
}

@Single
fun provideUserRepository(
    userMemoryRepository: UserMemoryRepository,
    userSettingsRepository: UserSettingsRepository
): UserRepository = if (BuildConfig.USE_MEMORY_DB) {
    userMemoryRepository
} else {
    userSettingsRepository
}
