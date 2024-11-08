package illyan.butler.core.network.ktor.http.di

import illyan.butler.core.local.room.dao.UserDao
import illyan.butler.core.network.ktor.http.setupClient
import illyan.butler.data.settings.AppRepository
import illyan.butler.error.ErrorManager
import io.ktor.client.HttpClient
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single

@ExperimentalSerializationApi
@Single
fun provideHttpClient(
    userDao: UserDao,
    appRepository: AppRepository,
    errorManager: ErrorManager
): HttpClient = HttpClient {
    setupClient(
        userDao = userDao,
        appRepository = appRepository,
        errorManager = errorManager
    )
}
