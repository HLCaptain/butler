package illyan.butler.ui.select_host_tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.manager.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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