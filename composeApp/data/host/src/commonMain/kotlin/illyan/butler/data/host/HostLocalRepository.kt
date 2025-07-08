package illyan.butler.data.host

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class HostLocalRepository @OptIn(ExperimentalSettingsApi::class) constructor(
    private val settings: FlowSettings
) : HostRepository {
    companion object {
        const val hostKey = "host"
    }

    override val currentHost = settings.getStringOrNullFlow(hostKey)

    override suspend fun upsertHostUrl(url: String?) {
        if (url.isNullOrBlank()) {
            settings.remove(hostKey)
        } else {
            settings.putString(hostKey, url)
        }
    }
}
