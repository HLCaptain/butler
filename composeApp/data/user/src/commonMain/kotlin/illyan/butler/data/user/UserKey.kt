package illyan.butler.data.user

import illyan.butler.shared.model.chat.Source
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface UserKey {
    sealed interface Read : UserKey {
        data class BySource(val source: Source.Server) : Read
    }

    data object Write : UserKey

    data class Delete(val userId: Uuid) : UserKey
}
