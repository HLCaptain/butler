package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.di.NamedCoroutineScopeIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class AppSettingsRepository(
    val settings: FlowSettings,
    @NamedCoroutineScopeIO private val coroutineScopeIO: CoroutineScope
) {
    val firstSignInHappenedYet = settings.getBooleanFlow("FIRST_SIGN_IN_HAPPENED_YET", false).stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        false
    )
}