package illyan.butler.di

import de.jensklingenberg.ktorfit.Ktorfit
import illyan.butler.data.ktorfit.api.AuthApi
import illyan.butler.data.ktorfit.api.ChatApi
import illyan.butler.data.ktorfit.api.MessageApi
import illyan.butler.data.ktorfit.api.ModelApi
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single
fun provideKtorfit(httpClient: HttpClient) = Ktorfit.Builder()
    .baseUrl("https://api.example.com/")
    .httpClient(httpClient)
    .build()

@Single
fun provideChatApi(ktorfit: Ktorfit) = ktorfit.create<ChatApi>()

@Single
fun provideModelApi(ktorfit: Ktorfit) = ktorfit.create<ModelApi>()

@Single
fun provideMessageApi(ktorfit: Ktorfit) = ktorfit.create<MessageApi>()

@Single
fun provideAuthApi(ktorfit: Ktorfit) = ktorfit.create<AuthApi>()
