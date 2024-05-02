package illyan.butler.repository.app

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.repository.user.UserRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class AppSettingsRepository(
    val settings: FlowSettings,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : AppRepository {
    @OptIn(ExperimentalSerializationApi::class)
    override val appSettings = settings.getStringOrNullFlow("APP_SETTINGS").map {
        try {
            if (it == null) {
                Napier.d { "No app settings found, creating one" }
                settings.putString("APP_SETTINGS", ProtoBuf.encodeToHexString(AppSettings.default))
                null
            } else {
                ProtoBuf.decodeFromHexString<AppSettings>(it).also {
                    Napier.d { "Decoded app settings: $it" }
                }
            }
        } catch (e: Exception) {
            Napier.e(e) { "Failed to decode app settings" }
            null
        }
    }.stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )

    override val firstSignInHappenedYet = settings.getBooleanOrNullFlow(UserRepository.FIRST_SIGN_IN_HAPPENED_YET).stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )

    override val isTutorialDone = settings.getBooleanOrNullFlow("IS_TUTORIAL_DONE").stateIn(
        coroutineScopeIO,
        SharingStarted.Eagerly,
        null
    )

    override suspend fun setTutorialDone(isTutorialDone: Boolean) = settings.putBoolean("IS_TUTORIAL_DONE", isTutorialDone)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun setUserPreferences(preferences: DomainPreferences) {
        settings.putString("APP_SETTINGS", ProtoBuf.encodeToHexString(appSettings.value?.copy(preferences = preferences)))
    }
}