package illyan.butler.data.resource

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface ResourceKey {
    sealed interface Read : ResourceKey {
        val source: Source
        @OptIn(ExperimentalUuidApi::class)
        data class ByResourceId(override val source: Source, val resourceId: Uuid) : Read
    }

    sealed interface Write : ResourceKey {
        data object Create : Write
        data object Upsert : Write
    }

    data class Delete(val resource: Resource) : ResourceKey
}
