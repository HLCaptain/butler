package illyan.butler.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainModel
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailState
import illyan.butler.ui.chat_details.ChatDetails
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.home.HamburgerButton
import illyan.butler.ui.new_chat.NewChat
import illyan.butler.ui.new_chat.NewChatState
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalHazeMaterialsApi::class)
@PreviewLightDark
@Composable
fun ChatDetailPreview() {
    ButlerTheme {
        Surface {
            CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
                ChatDetail(
                    state = ChatDetailState(
                        chat = DomainChat(
                            id = "1",
                            created = Clock.System.now().minus(3.days).toEpochMilliseconds(),
                            name = "Chat 1",
                            summary = "Chat 1 summary",
                            ownerId = "1",
                        ),
                        messages = listOf(
                            DomainMessage(
                                id = "1",
                                senderId = "1",
                                message = "Hello",
                                time = Clock.System.now().minus(2.days).toEpochMilliseconds(),
                                chatId = "1",
                            ),
                            DomainMessage(
                                id = "2",
                                senderId = "Cook GPT",
                                message = "Hi, what recipe would you like to cook today?",
                                time = Clock.System.now().minus(1.days).toEpochMilliseconds(),
                                chatId = "1",
                            ),
                            DomainMessage(
                                id = "3",
                                senderId = "1",
                                message = "I would like to cook a pizza",
                                time = Clock.System.now().minus(1.days - 1.seconds).toEpochMilliseconds(),
                                chatId = "1",
                            ),
                            DomainMessage(
                                id = "4",
                                senderId = "Cook GPT",
                                message = "Great! Here is a recipe for pizza\n\nIngredients:\n- 1 pizza dough\n- 1 cup of tomato sauce\n- 1 cup of mozzarella cheese\n\nInstructions:\n1. Preheat the oven to 400°F\n2. Roll out the pizza dough\n3. Spread the tomato sauce on the dough\n4. Sprinkle the cheese on top\n5. Bake for 15 minutes\n\nEnjoy your pizza!",
                                time = Clock.System.now().minus(1.days - 2.seconds).toEpochMilliseconds(),
                                chatId = "1",
                            ),
                            DomainMessage(
                                id = "5",
                                senderId = "1",
                                message = "Thank you!",
                                time = Clock.System.now().minus(1.days - 3.seconds).toEpochMilliseconds(),
                                chatId = "1",
                            )
                        ).sortedByDescending { it.time },
                        isRecording = false,
                        sounds = emptyMap(),
                        playingAudio = null,
                        images = emptyMap(),
                    ),
                    sendMessage = {},
                    toggleRecord = {},
                    sendImage = { _, _ -> },
                    playAudio = {},
                    stopAudio = {},
                    openChatDetails = {},
                    navigationIcon = { HamburgerButton() },
                    isChatDetailsOpenRatio = 0.0f
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@PreviewLightDark
@Composable
fun ChatListPreview() {
    ButlerTheme {
        Surface {
            CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
                ChatList(
                    chats = listOf(
                        DomainChat(
                            id = "1",
                            created = Clock.System.now().minus(3.days).toEpochMilliseconds(),
                            name = "Chat 1",
                            summary = "Chat 1 summary",
                            ownerId = "1",
                        ),
                        DomainChat(
                            id = "2",
                            created = Clock.System.now().minus(2.days).toEpochMilliseconds(),
                            name = "Chat 2",
                            summary = "Chat 2 summary",
                            ownerId = "1",
                        ),
                        DomainChat(
                            id = "3",
                            created = Clock.System.now().minus(1.days).toEpochMilliseconds(),
                            name = "Chat 3",
                            summary = "Chat 3 summary",
                            ownerId = "1",
                        )
                    ),
                    deleteChat = {},
                    openChat = {},
                    selectedChat = "1",
                    deviceOnlyChatIds = listOf("2")
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun ChatDetailsPreview() {
    ButlerTheme {
        Surface {
            ChatDetails(
                chat = DomainChat(
                    id = "1",
                    created = Clock.System.now().minus(3.days).toEpochMilliseconds(),
                    name = "Chat 1",
                    summary = "Chat 1 summary",
                    ownerId = "1",
                ),
                currentUserId = "1"
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NewChatPreview() {
    ButlerTheme {
        Surface {
            NewChat(
                state = NewChatState(
                    serverModels = listOf(
                        DomainModel(
                            name = "Cook GPT",
                            id = "Cook GPT",
                            ownedBy = "Chef AI",
                            endpoint = "https://api.chef.ai"
                        ),
                        DomainModel(
                            name = "Bartender",
                            id = "Bartender",
                            ownedBy = "Bartender AI",
                            endpoint = "https://api.bartender.ai"
                        ),
                        DomainModel(
                            name = "Doctor",
                            id = "Doctor",
                            ownedBy = "Doctor AI",
                            endpoint = "https://api.doctor.ai"
                        )
                    )
                ),
                selectModel = { _, _, _ -> },
            )
        }
    }
}
