package illyan.butler.di

import illyan.butler.config.BuildConfig
import illyan.butler.data.ktor.http.AuthHttpDataSource
import illyan.butler.data.ktor.http.ChatHttpDataSource
import illyan.butler.data.ktor.http.HostHttpDataSource
import illyan.butler.data.ktor.http.MessageHttpDataSource
import illyan.butler.data.ktor.http.ModelHttpDataSource
import illyan.butler.data.ktor.http.ResourceHttpDataSource
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.datasource.HostNetworkDataSource
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import org.koin.core.annotation.Single

@Single
fun provideResourceNetworkDataSource(
    resourceHttpDataSource: ResourceHttpDataSource,
//    resourceRpcDataSource: ResourceRpcDataSource
): ResourceNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        resourceHttpDataSource
    } else {
        resourceHttpDataSource
    }
}

@Single
fun provideAuthNetworkDataSource(
    authHttpDataSource: AuthHttpDataSource,
//    authRpcDataSource: AuthRpcDataSource
): AuthNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        authHttpDataSource
    } else {
        authHttpDataSource
    }
}

@Single
fun provideChatNetworkDataSource(
    chatHttpDataSource: ChatHttpDataSource,
//    chatRpcDataSource: ChatRpcDataSource
): ChatNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        chatHttpDataSource
    } else {
        chatHttpDataSource
    }
}

@Single
fun provideHostNetworkDataSource(
    hostHttpDataSource: HostHttpDataSource,
//    hostRpcDataSource: HostRpcDataSource
): HostNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        hostHttpDataSource
    } else {
        hostHttpDataSource
    }
}

@Single
fun provideMessageNetworkDataSource(
    messageHttpDataSource: MessageHttpDataSource,
//    messageRpcDataSource: MessageRpcDataSource
): MessageNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        messageHttpDataSource
    } else {
        messageHttpDataSource
    }
}

@Single
fun provideModelNetworkDataSource(
    modelHttpDataSource: ModelHttpDataSource,
//    modelRpcDataSource: ModelRpcDataSource
): ModelNetworkDataSource {
    return if (BuildConfig.USE_RPC) {
        // TODO: Use RpcDataSource when it is implemented
        modelHttpDataSource
    } else {
        modelHttpDataSource
    }
}