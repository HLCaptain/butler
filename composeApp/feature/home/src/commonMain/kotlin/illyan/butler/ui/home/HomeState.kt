package illyan.butler.ui.home

import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.DomainError
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class HomeState @OptIn(ExperimentalUuidApi::class) constructor(
    val signedInUserId: Uuid? = null,
    val errors: List<DomainError> = listOf(),
    val chats: List<Chat> = listOf(),
    val credentials: List<ApiKeyCredential> = listOf(),
)
