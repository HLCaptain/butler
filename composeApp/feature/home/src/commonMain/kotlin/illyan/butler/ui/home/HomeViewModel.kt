package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.config.BuildConfig
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.error.ErrorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class)
@KoinViewModel
class HomeViewModel(
    authManager: AuthManager,
    private val chatManager: ChatManager,
    credentialRepository: CredentialRepository,
    errorManager: ErrorManager,
) : ViewModel() {
    private val _serverErrors = MutableStateFlow<List<Pair<String, DomainErrorResponse>>>(listOf())
    private val _appErrors = MutableStateFlow<List<DomainErrorEvent>>(listOf())

    init {
        if (BuildConfig.NO_CONFIG_SETUP) {
            viewModelScope.launch {
                val credentials = credentialRepository.apiKeyCredentials.first()
                if (credentials.orEmpty().none { it.providerUrl.contains("openrouter.ai") }) {
                    // This is a 1$ API key for openrouter.ai, but I don't have any credits left
                    // so you can only user ":free" models.
                    // Encoded in Base64 TWICE(!) so bots are less likely to scrape it.
                    val encodedKey = Base64.decode("YzJzdGIzSXRkakV0Wmpnd05EVm1aRFk1WkRBeVlXVmlNREV5TVdReVkyTmlZak5oTWpSbFlqSTNNREF6TmpVeFpEVXdPVEJpTVdKa09HWTNZbVpoWTJNNU1tTmlZV0l3WVE9PQ==").toString(Charsets.UTF_8)
                    credentialRepository.upsertApiKeyCredential(
                        ApiKeyCredential(
                            "OpenRouter",
                            "https://openrouter.ai/api/v1/",
                            apiKey = Base64.decode(encodedKey).toString(Charsets.UTF_8).trim()
                        )
                    )
                }
            }
        }
    }

    val state = combine(
        authManager.signedInUserId,
        authManager.clientId,
        _serverErrors,
        _appErrors,
        chatManager.userChats,
        chatManager.deviceChats,
        credentialRepository.apiKeyCredentials
    ) { flows ->
        val signedInUserId = flows[0] as String?
        val clientId = flows[1] as String?
        val serverErrors = flows[2] as List<Pair<String, DomainErrorResponse>>
        val appErrors = flows[3] as List<DomainErrorEvent>
        val userChats = flows[4] as List<DomainChat>
        val deviceChats = flows[5] as List<DomainChat>
        val credentials = flows[6] as List<ApiKeyCredential>? ?: emptyList()
        HomeState(
            signedInUserId = signedInUserId,
            clientId = clientId,
            serverErrors = serverErrors,
            appErrors = appErrors,
            userChats = userChats,
            deviceChats = deviceChats,
            credentials = credentials
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            errorManager.serverErrors.collectLatest { response ->
                _serverErrors.update { it + (Uuid.random().toString() to response) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatManager.deleteChat(chatId)
        }
    }
}
