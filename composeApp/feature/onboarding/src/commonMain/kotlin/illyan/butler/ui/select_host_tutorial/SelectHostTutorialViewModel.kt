package illyan.butler.ui.select_host_tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.host.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SelectHostTutorialViewModel(
    hostManager: HostManager
) : ViewModel() {
    val state = hostManager.currentHost.map {
        SelectHostTutorialState(it != null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SelectHostTutorialState()
    )
}