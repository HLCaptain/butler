package illyan.butler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.auth.AuthManager
import illyan.butler.chat.ChatManager
import illyan.butler.config.BuildConfig
import illyan.butler.data.credential.CredentialRepository
import illyan.butler.data.error.ErrorRepository
import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.DomainError
import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class, ExperimentalTime::class)
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
                            "https://openrouter.ai/api/v1/",
                            apiKey = Base64.decode(encodedKey).toString(Charsets.UTF_8).trim()
                        )
                    )
                }
            }
        }
    }

    val state = combine(
        authManager.signedInServers.map { it.firstOrNull() },
        errors,
        chatManager.chats,
        credentialRepository.apiKeyCredentials,
    ) { flows ->
        val flows = flows.toMutableList()
        val signedInUserId = flows.removeAt(0) as Uuid?
        val errors = flows.removeAt(0) as List<DomainError>
        val chats = flows.removeAt(0) as List<Chat>
        val credentials = flows.removeAt(0) as List<ApiKeyCredential>? ?: emptyList()
        Napier.v {
            """
            signedInUserId: $signedInUserId
            numberOfUserChats: ${chats.filter { it.source is Source.Server }.size}
            numberOfDeviceChats: ${chats.filter { it.source is Source.Device }.size}
            errors: $errors
            numberOfCredentials: ${credentials.size}
            """.trimIndent()
        }
        HomeState(
            signedInUserId = signedInUserId,
            chats = chats,
            errors = errors,
            credentials = credentials,
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

    fun clearError(errorId: Uuid) {
        viewModelScope.launch(Dispatchers.IO) {
            errors.update { it.filter { error -> error.id != errorId } }
        }
    }

    fun removeLastError() {
        viewModelScope.launch(Dispatchers.IO) {
            errors.update { it.dropLast(1) }
        }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            chatManager.deleteChat(chat)
        }
    }
}
