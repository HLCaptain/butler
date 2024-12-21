package illyan.butler.data.host

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class HostMemoryRepository : HostRepository {
    private val _currentHost = MutableStateFlow<String?>(null)
    override val currentHost = _currentHost.asStateFlow()

    override suspend fun upsertHostUrl(url: String?) {
        _currentHost.update { url }
    }
}