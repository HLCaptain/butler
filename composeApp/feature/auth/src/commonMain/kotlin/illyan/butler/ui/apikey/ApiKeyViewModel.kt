package illyan.butler.ui.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import illyan.butler.host.HostManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ApiKeyViewModel(private val hostManager: HostManager) : ViewModel() {
    val apiKeyCredentials = hostManager.currentCredentials.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    val modelsForCredential = MutableStateFlow<Map<ApiKeyCredential, List<DomainModel>>>(emptyMap())

    fun testEndpointForCredentials(credential: ApiKeyCredential) {
        viewModelScope.launch {
            val models = hostManager.testApiKeyCredentials(credential)
            modelsForCredential.update { it + (credential to models) }
        }
    }

    fun addApiKeyCredential(credential: ApiKeyCredential) {
        viewModelScope.launch {
            hostManager.addApiKeyCredential(credential)
        }
    }
}
