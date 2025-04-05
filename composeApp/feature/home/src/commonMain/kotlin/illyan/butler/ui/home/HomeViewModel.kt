package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.config.BuildConfig
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.error.ErrorRepository
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainError
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class)
@KoinViewModel
class HomeViewModel(
    authManager: AuthManager,
    private val chatManager: ChatManager,
    credentialRepository: CredentialRepository,
    errorRepository: ErrorRepository,
) : ViewModel() {
    private val errors = MutableStateFlow(emptyList<DomainError>())
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
        errors,
        chatManager.userChats,
        chatManager.deviceChats,
        credentialRepository.apiKeyCredentials
    ) { flows ->
        val signedInUserId = flows[0] as String?
        val clientId = flows[1] as String?
        val errors = flows[2] as List<DomainError>
        val userChats = flows[3] as List<DomainChat>
        val deviceChats = flows[4] as List<DomainChat>
        val credentials = flows[5] as List<ApiKeyCredential>? ?: emptyList()
        Napier.v {
            """
            signedInUserId: $signedInUserId
            numberOfUserChats: ${userChats.size}
            numberOfDeviceChats: ${deviceChats.size}
            errors: $errors
            numberOfCredentials: ${credentials.size}
            """.trimIndent()
        }
        HomeState(
            signedInUserId = signedInUserId,
            clientId = clientId,
            errors = errors,
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
            errorRepository.errorEventFlow.collect { error ->
                Napier.v { "Error event received: $error" }
                errors.update { it + error }
            }
        }
    }

    fun clearError(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            errors.update { it.filter { error -> error.id != id } }
        }
    }

    fun removeLastError() {
        viewModelScope.launch(Dispatchers.IO) {
            errors.update { it.dropLast(1) }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatManager.deleteChat(chatId)
        }
    }
}
