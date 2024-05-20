package illyan.butler.ui.usage_tutorial

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.AppManager
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class UsageTutorialScreenModel(
    private val appManager: AppManager
) : ScreenModel {
    fun setTutorialDone() {
        screenModelScope.launch {
            appManager.setTutorialDone()
        }
    }
}