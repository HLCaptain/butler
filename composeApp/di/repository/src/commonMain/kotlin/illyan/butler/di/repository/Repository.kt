package illyan.butler.di.repository

import illyan.butler.config.BuildConfig
import illyan.butler.data.chat.ChatMemoryRepository
import illyan.butler.data.chat.ChatRepository
import illyan.butler.data.chat.ChatStoreRepository
import illyan.butler.data.credential.CredentialLocalRepository
import illyan.butler.data.credential.CredentialMemoryRepository
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.error.ErrorMemoryRepository
import illyan.butler.data.error.ErrorRepository
import illyan.butler.data.host.HostLocalRepository
import illyan.butler.data.host.HostMemoryRepository
import illyan.butler.data.host.HostRepository
import illyan.butler.data.message.MessageMemoryRepository
import illyan.butler.data.message.MessageRepository
import illyan.butler.data.message.MessageStoreRepository
import illyan.butler.data.model.ModelMemoryRepository
import illyan.butler.data.model.ModelNetworkRepository
import illyan.butler.data.model.ModelRepository
import illyan.butler.data.resource.ResourceMemoryRepository
import illyan.butler.data.resource.ResourceRepository
import illyan.butler.data.resource.ResourceStoreRepository
import illyan.butler.data.settings.AppMemoryRepository
import illyan.butler.data.settings.AppRepository
import illyan.butler.data.settings.AppSettingsLocalRepository
import illyan.butler.data.user.UserMemoryRepository
import illyan.butler.data.user.UserRepository
import illyan.butler.data.user.UserStoreRepository
import org.koin.core.annotation.Single

@Single
fun provideAppRepository(
    appMemoryRepository: AppMemoryRepository,
    roomAppSettingsRepository: AppSettingsLocalRepository
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
    hostLocalRepository: HostLocalRepository
): HostRepository = if (BuildConfig.USE_MEMORY_DB) {
    hostMemoryRepository
} else {
    hostLocalRepository
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
    userStoreRepository: UserStoreRepository
): UserRepository = if (BuildConfig.USE_MEMORY_DB) {
    userMemoryRepository
} else {
    userStoreRepository
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
fun provideCredentialRepository(
    credentialMemoryRepository: CredentialMemoryRepository,
    credentialLocalRepository: CredentialLocalRepository
): CredentialRepository = if (BuildConfig.USE_MEMORY_DB) {
    credentialMemoryRepository
} else {
    credentialLocalRepository
}
