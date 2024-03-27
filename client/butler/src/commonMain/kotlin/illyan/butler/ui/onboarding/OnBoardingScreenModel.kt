package illyan.butler.ui.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.AppManager
import illyan.butler.manager.AuthManager
import illyan.butler.manager.HostManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class OnBoardingScreenModel(
    private val appManager: AppManager,
    authManager: AuthManager,
    hostManager: HostManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher
) : ScreenModel {
    val state = combine(
        hostManager.currentHost,
        authManager.isUserSignedIn,
        appManager.isTutorialDone
    ) { currentHost, isUserSignedIn, isTutorialDone ->
        OnBoardingState(
            isHostSelected = currentHost != null,
            isUserSignedIn = isUserSignedIn,
            isTutorialDone = isTutorialDone
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = OnBoardingState()
    )

    fun setTutorialDone() {
        // Use IO dispatcher if Voyager crashes unexpectedly
        screenModelScope.launch(dispatcherIO) {
            appManager.setTutorialDone()
        }
    }
}