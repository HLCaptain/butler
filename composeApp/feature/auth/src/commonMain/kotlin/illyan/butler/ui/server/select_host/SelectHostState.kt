package illyan.butler.ui.server.select_host

data class SelectHostState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean? = null,
    val currentHost: String? = null
)