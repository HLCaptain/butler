package illyan.butler.ui.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.DomainModel
import illyan.butler.host.HostManager
import illyan.butler.model.ModelManager
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ApiKeyViewModel(
    private val hostManager: HostManager,
    private val modelManager: ModelManager
) : ViewModel() {
    val apiKeyCredentials = hostManager.currentCredentials.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val healthyCredentials = modelManager.healthyHostCredentials.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val modelsForCredential = MutableStateFlow<Map<ApiKeyCredential, List<DomainModel>>>(emptyMap())

    fun testEndpointForCredential(credential: ApiKeyCredential) {
        viewModelScope.launch {
            val models = hostManager.testApiKeyCredential(credential)
            modelsForCredential.update { it + (credential to models) }
        }
    }

    fun addApiKeyCredential(credential: ApiKeyCredential) {
        viewModelScope.launch {
            hostManager.addApiKeyCredential(credential)
        }
    }

    fun deleteApiKeyCredential(apiKeyCredential: ApiKeyCredential) {
        viewModelScope.launch {
            hostManager.deleteApiKeyCredential(apiKeyCredential)
        }
    }
}
