package illyan.butler.data.host

import kotlinx.coroutines.flow.Flow

interface HostRepository {
    val currentHost: Flow<String?>
    suspend fun upsertHostUrl(url: String?)
}
