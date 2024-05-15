package illyan.butler.ui.error

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.manager.ErrorManager
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ErrorScreenModel(
    private val errorManager: ErrorManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    private val _serverErrors = MutableStateFlow<List<Pair<String, DomainErrorResponse>>>(listOf())
    private val _appErrors = MutableStateFlow<List<DomainErrorEvent>>(listOf())
    val state = combine(
        _serverErrors,
        _appErrors
    ) { serverErrors, appErrors ->
        ErrorScreenState(serverErrors, appErrors)
    }.stateIn(
        screenModelScope,
        SharingStarted.Eagerly,
        ErrorScreenState()
    )

    init {
        screenModelScope.launch(dispatcherIO) {
            errorManager.serverErrors.collectLatest { response ->
                _serverErrors.update { it + (randomUUID() to  response) }
            }
        }
        screenModelScope.launch(dispatcherIO) {
            errorManager.appErrors.collectLatest { error ->
                _appErrors.update { it + error }
            }
        }
    }

    fun clearError(id: String) {
        _serverErrors.update { errors ->
            errors.filter { it.first != id }
        }
        _appErrors.update { errors ->
            errors.filter { it.id != id }
        }
    }

    fun removeLastError() {
        screenModelScope.launch(dispatcherIO) {
            val latestServerErrorId = _serverErrors.first().maxByOrNull { it.second.timestamp }?.first
            val latestAppErrorId = _appErrors.first().maxByOrNull { it.timestamp }?.id
            if (latestServerErrorId != null && latestAppErrorId != null) {
                if (latestServerErrorId > latestAppErrorId) {
                    clearError(latestServerErrorId)
                } else {
                    clearError(latestAppErrorId)
                }
            } else if (latestServerErrorId != null) {
                clearError(latestServerErrorId)
            } else if (latestAppErrorId != null) {
                clearError(latestAppErrorId)
            }
        }
    }
}