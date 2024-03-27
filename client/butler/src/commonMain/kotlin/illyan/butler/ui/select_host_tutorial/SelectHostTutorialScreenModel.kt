package illyan.butler.ui.select_host_tutorial

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.HostManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Factory

@Factory
class SelectHostTutorialScreenModel(
    hostManager: HostManager
) : ScreenModel {
    val state = hostManager.currentHost.map {
        SelectHostTutorialState(it != null)
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SelectHostTutorialState()
    )
}