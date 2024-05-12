package illyan.butler.ui.welcome

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.AppManager
import illyan.butler.repository.user.UserRepository
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class WelcomeScreenModel(
    private val appManager: AppManager,
    private val userRepository: UserRepository
) : ScreenModel {
    private val dataFlow1 = MutableStateFlow(false)
    private val dataFlow2 = MutableStateFlow(true)

    val state = combine(
        dataFlow1,
        dataFlow2
    ) { flow1, flow2 ->
        WelcomeState(
            dataFlow1 = flow1,
            dataFlow2 = flow2
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = WelcomeState(
            dataFlow1 = dataFlow1.value,
            dataFlow2 = dataFlow2.value
        )
    )

    fun skipTutorialAndLogin() {
        screenModelScope.launch {
            appManager.setTutorialDone()
            val userName = "illyan${randomUUID().takeLast(8)}"
            userRepository.signUpAndLogin(
                "$userName@nest.ai",
                "password",
                userName
            )
        }
    }
}