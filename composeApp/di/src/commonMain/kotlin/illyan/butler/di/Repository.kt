package illyan.butler.di

import illyan.butler.config.BuildConfig
import illyan.butler.data.error.ErrorMemoryRepository
import illyan.butler.data.error.ErrorRepository
import illyan.butler.data.model.ModelMemoryRepository
import illyan.butler.data.model.ModelNetworkRepository
import illyan.butler.data.model.ModelRepository
import illyan.butler.data.permission.MemoryPermissionRepository
import illyan.butler.data.permission.PermissionRepository
import illyan.butler.data.permission.getPlatformPermissionRepository
import illyan.butler.data.settings.AppMemoryRepository
import illyan.butler.data.settings.AppRepository
import illyan.butler.data.settings.AppSettingsRoomRepository
import illyan.butler.data.chat.ChatMemoryRepository
import illyan.butler.data.chat.ChatRepository
import illyan.butler.repository.chat.ChatStoreRepository
import illyan.butler.repository.host.HostMemoryRepository
import illyan.butler.repository.host.HostRepository
import illyan.butler.repository.host.HostRoomRepository
import illyan.butler.data.message.MessageMemoryRepository
import illyan.butler.repository.message.MessageRepository
import illyan.butler.repository.message.MessageStoreRepository
import illyan.butler.data.resource.ResourceMemoryRepository
import illyan.butler.repository.resource.ResourceRepository
import illyan.butler.repository.resource.ResourceStoreRepository
import illyan.butler.repository.user.UserMemoryRepository
import illyan.butler.repository.user.UserRepository
import illyan.butler.repository.user.UserRoomRepository
import org.koin.core.annotation.Single

@Single
fun provideAppRepository(
    appMemoryRepository: AppMemoryRepository,
    roomAppSettingsRepository: AppSettingsRoomRepository
): AppRepository = if (BuildConfig.USE_MEMORY_DB) {
    appMemoryRepository
} else {
    roomAppSettingsRepository
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
    hostRoomRepository: HostRoomRepository
): HostRepository = if (BuildConfig.USE_MEMORY_DB) {
    hostMemoryRepository
} else {
    hostRoomRepository
}

@Single
fun provideErrorRepository(
    errorMemoryRepository: ErrorMemoryRepository,
): ErrorRepository = if (BuildConfig.USE_MEMORY_DB) {
    errorMemoryRepository
} else {
    errorMemoryRepository // TODO: use local persistency, like Room to log errors, then upload to server
}

@Single
fun provideUserRepository(
    userMemoryRepository: UserMemoryRepository,
    userRoomRepository: UserRoomRepository
): UserRepository = if (BuildConfig.USE_MEMORY_DB) {
    userMemoryRepository
} else {
    userRoomRepository
}

@Single
fun provideResourceRepository(
    resourceMemoryRepository: ResourceMemoryRepository,
    resourceStoreRepository: ResourceStoreRepository
): ResourceRepository = if (BuildConfig.USE_MEMORY_DB) {
    resourceMemoryRepository
} else {
    resourceStoreRepository
}

@Single
fun providePermissionRepository(
    memoryPermissionRepository: MemoryPermissionRepository
): PermissionRepository = if (BuildConfig.USE_MEMORY_DB) {
    memoryPermissionRepository
} else {
    getPlatformPermissionRepository()
}
