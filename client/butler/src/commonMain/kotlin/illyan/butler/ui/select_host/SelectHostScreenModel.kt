package illyan.butler.ui.select_host

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.HostManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class SelectHostScreenModel(
    private val hostManager: HostManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    val state = combine(
        hostManager.isConnectingToHost,
        hostManager.isConnectedToHost,
        hostManager.currentHost
    ) { isConnecting, isConnected, currentHost ->
        SelectHostState(
            isConnecting = isConnecting,
            isConnected = isConnected,
            currentHost = currentHost ?: ""
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SelectHostState()
    )

    fun testAndSelectHost(url: String) {
        screenModelScope.launch(dispatcherIO) {
            hostManager.testAndSelectHost(url)
        }
    }

    fun testHost(url: String) {
        screenModelScope.launch(dispatcherIO) {
            hostManager.testHost(url)
        }
    }
}