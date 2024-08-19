package illyan.butler.di

import illyan.butler.data.ktor.rpc.service.AuthService
import illyan.butler.data.ktor.rpc.service.ChatService
import illyan.butler.data.ktor.rpc.service.HostService
import illyan.butler.data.ktor.rpc.service.MessageService
import illyan.butler.data.ktor.rpc.service.ModelService
import illyan.butler.data.ktor.rpc.service.ResourceService
import illyan.butler.repository.host.HostRepository
import io.ktor.client.HttpClient
import kotlinx.rpc.RPCClient
import kotlinx.rpc.client.withService
import org.koin.core.annotation.Single

@Single
fun provideRpcClient(
    hostRepository: HostRepository,
    httpClient: HttpClient
): RPCClient {
    TODO("Not yet implemented")
    // TODO: handle dynamic host changes
}

@Single fun provideAuthService(rpcClient: RPCClient): AuthService = rpcClient.withService()
@Single fun provideChatService(rpcClient: RPCClient): ChatService = rpcClient.withService()
@Single fun provideHostService(rpcClient: RPCClient): HostService = rpcClient.withService()
@Single fun provideMessageService(rpcClient: RPCClient): MessageService = rpcClient.withService()
@Single fun provideModelService(rpcClient: RPCClient): ModelService = rpcClient.withService()
@Single fun provideResourceService(rpcClient: RPCClient): ResourceService = rpcClient.withService()