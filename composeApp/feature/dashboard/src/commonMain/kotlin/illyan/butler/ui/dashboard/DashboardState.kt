package illyan.butler.ui.dashboard

import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.User
import illyan.butler.shared.model.chat.PromptConfiguration
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class DashboardState(
    val users: PersistentSet<User> = persistentSetOf(),
    val selectedUser: User? = null,
    val appSettings: AppSettings? = null,
    val selectedPromptConfiguration: PromptConfiguration? = null,
)
