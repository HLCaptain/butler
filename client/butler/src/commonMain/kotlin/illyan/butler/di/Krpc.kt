package illyan.butler.di

import illyan.butler.data.ktor.rpc.service.AuthService
import illyan.butler.data.ktor.rpc.service.ChatService
import illyan.butler.data.ktor.rpc.service.HostService
import illyan.butler.data.ktor.rpc.service.MessageService
import illyan.butler.data.ktor.rpc.service.ModelService
import illyan.butler.data.ktor.rpc.service.ResourceService
import illyan.butler.repository.host.HostRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.rpc.RPCClient
import kotlinx.rpc.client.withService
import kotlinx.rpc.transport.ktor.client.rpc
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
fun provideRpcClient(
    hostRepository: HostRepository,
    httpClient: HttpClient
): Flow<RPCClient?> = hostRepository.currentHost.map { host ->
    host?.let { httpClient.rpc("$host/service") }
}

@Single fun provideAuthService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<AuthService?> = rpcClient
    .map { it?.withService<AuthService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

@Single fun provideChatService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<ChatService?> = rpcClient
    .map { it?.withService<ChatService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

@Single fun provideHostService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<HostService?> = rpcClient
    .map { it?.withService<HostService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

@Single fun provideMessageService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<MessageService?> = rpcClient
    .map { it?.withService<MessageService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

@Single fun provideModelService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<ModelService?> = rpcClient
    .map { it?.withService<ModelService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

@Single fun provideResourceService(
    rpcClient: Flow<RPCClient?>,
    @Named(KoinNames.CoroutineScopeIO) coroutineScopeIO: CoroutineScope
): StateFlow<ResourceService?> = rpcClient
    .map { it?.withService<ResourceService>() }
    .stateIn(coroutineScopeIO, SharingStarted.Eagerly, null)

