package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.AppSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface AppRepository {
    val appSettings: StateFlow<AppSettings?>
    val firstSignInHappenedYet: StateFlow<Boolean>
    val isTutorialDone: StateFlow<Boolean>

    suspend fun setTutorialDone(isTutorialDone: Boolean)
}