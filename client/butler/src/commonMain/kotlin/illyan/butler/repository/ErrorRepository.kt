package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.ErrorEvent
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.ErrorState
import illyan.butler.getOsName
import illyan.butler.getPlatformName
import illyan.butler.getSystemMetadata
import illyan.butler.util.log.randomUUID
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentLength
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

interface ErrorRepository {
    val appErrorEventFlow: SharedFlow<DomainErrorEvent>
    val serverErrorEventFlow: SharedFlow<DomainErrorResponse>

    suspend fun reportError(throwable: Throwable)
    suspend fun reportError(response: HttpResponse)
}