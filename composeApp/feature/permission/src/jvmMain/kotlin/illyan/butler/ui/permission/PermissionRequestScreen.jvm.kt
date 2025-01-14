package illyan.butler.ui.permission

import org.jetbrains.compose.resources.StringResource

// Desktop platform doesn't need permission
actual val platformSpecificPermissions: Map<String, Pair<StringResource?, StringResource?>>
    get() = emptyMap()