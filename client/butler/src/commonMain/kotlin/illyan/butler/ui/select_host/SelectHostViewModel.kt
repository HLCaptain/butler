package illyan.butler.ui.select_host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.HostManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectHostViewModel(
    private val hostManager: HostManager,
) : ViewModel() {
    private val isConnectedToHost = MutableStateFlow<Boolean?>(null)
    val state = combine(
        hostManager.isConnectingToHost,
        isConnectedToHost,
        hostManager.currentHost
    ) { isConnecting, isConnected, currentHost ->
        SelectHostState(
            isConnecting = isConnecting,
            isConnected = isConnected,
            currentHost = currentHost
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SelectHostState()
    )

    fun testAndSelectHost(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isConnectedToHost.update { hostManager.testAndSelectHost(url) }
        }
    }

    fun testHost(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isConnectedToHost.update { hostManager.testHost(url) }
        }
    }
}