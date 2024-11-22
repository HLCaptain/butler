package illyan.butler.data.resource

sealed class ResourceKey {
    sealed class Read : ResourceKey() {
        data class ByResourceId(val resourceId: String) : Read()
    }

    sealed class Write : ResourceKey() {
        data object Create : Write()
        data object Upsert : Write()
    }

    sealed class Delete : ResourceKey() {
        data class ByResourceId(val resourceId: String) : Delete()
        data object All : Delete()
    }
}