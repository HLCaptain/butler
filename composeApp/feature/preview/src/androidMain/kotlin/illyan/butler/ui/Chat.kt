package illyan.butler.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailState
import illyan.butler.ui.chat_details.ChatDetails
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.new_chat.NewChat
import illyan.butler.ui.new_chat.NewChatState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
@PreviewLightDark
@Composable
fun ChatDetailPreview() {
    ButlerTheme {
        Surface {
            CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
                val chatId = remember { Uuid.random() }
                val deviceId = remember { Uuid.random() }
                ChatDetail(
                    state = ChatDetailState(
                        chat = Chat(
                            id = chatId,
                            source = Source.Device(deviceId),
                        ),
                        messages = listOf(
                            Message(
                                sender = SenderType.System,
                                content = "You are an AI assistant specialized in cooking recipes. Your task is to help users find and create recipes based on their preferences.",
                                chatId = chatId,
                                source = Source.Device(deviceId),
                            ),
                            Message(
                                sender = SenderType.Ai(source = AiSource.Local("cook-gpt")),
                                content = "Hi, what recipe would you like to cook today?",
                                createdAt = Clock.System.now().minus(1.days),
                                chatId = chatId,
                                source = Source.Device(deviceId)
                            ),
                            Message(
                                sender = SenderType.User(source = Source.Device(deviceId)),
                                content = "I would like to cook a pizza",
                                createdAt = Clock.System.now().minus(1.days - 1.seconds),
                                chatId = chatId,
                                source = Source.Device(deviceId)
                            ),
                            Message(
                                sender = SenderType.Ai(source = AiSource.Local("cook-gpt")),
                                content = "Great! Here is a recipe for pizza\n\nIngredients:\n- 1 pizza dough\n- 1 cup of tomato sauce\n- 1 cup of mozzarella cheese\n\nInstructions:\n1. Preheat the oven to 400°F\n2. Roll out the pizza dough\n3. Spread the tomato sauce on the dough\n4. Sprinkle the cheese on top\n5. Bake for 15 minutes\n\nEnjoy your pizza!",
                                createdAt = Clock.System.now().minus(1.days - 2.seconds),
                                chatId = chatId,
                                source = Source.Device(deviceId)
                            ),
                            Message(
                                sender = SenderType.User(source = Source.Device(deviceId)),
                                content = "Thank you!",
                                createdAt = Clock.System.now().minus(1.days - 3.seconds),
                                chatId = chatId,
                                source = Source.Device(deviceId)
                            )
                        ).sortedByDescending { it.createdAt.toEpochMilliseconds() }
                    ),
                    sendMessage = {},
                    sendImage = { _, _ -> },
                    toggleRecord = {},
                    playAudio = {},
                    stopAudio = {},
                    refreshChat = {},
                    sendError = {},
                    navigateToModelSelection = {},
                    navigateToChatSettings = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalTime::class, ExperimentalUuidApi::class)
@PreviewLightDark
@Composable
fun ChatListPreview() {
    ButlerTheme {
        Surface {
            CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
                val deviceId = remember { Uuid.random() }
                val selectedChatId = remember { Uuid.random() }
                ChatList(
                    chats = listOf(
                        Chat(
                            createdAt = Clock.System.now().minus(3.days),
                            title = "Chat 1",
                            summary = "Chat 1 summary",
                            source = Source.Device(deviceId),
                        ),
                        Chat(
                            id = selectedChatId,
                            createdAt = Clock.System.now().minus(2.days),
                            title = "Chat 2",
                            summary = "Chat 2 summary",
                            source = Source.Device(deviceId),
                        ),
                        Chat(
                            createdAt = Clock.System.now().minus(1.days),
                            title = "Chat 3",
                            summary = "Chat 3 summary",
                            source = Source.Device(deviceId),
                        )
                    ),
                    deleteChat = {},
                    openChat = {},
                    selectedChat = selectedChatId,
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@PreviewLightDark
@Composable
fun ChatDetailsPreview() {
    ButlerTheme {
        Surface {
            ChatDetails(
                chat = Chat(
                    id = remember { Uuid.random() },
                    createdAt = Clock.System.now().minus(2.days),
                    title = "Chat Title",
                    summary = "This is a summary of the chat.",
                    source = Source.Device(remember { Uuid.random() }),
                ),
                alternativeModels = listOf(
                    AiSource.Local("cook-gpt"),
                    AiSource.Local("bartender"),
                    AiSource.Local("doctor")
                ),
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@PreviewLightDark
@Composable
fun NewChatPreview() {
    ButlerTheme {
        Surface {
            NewChat(
                state = NewChatState(
                    aiSources = listOf(
                        AiSource.Local("cook-gpt"),
                        AiSource.Local("bartender"),
                        AiSource.Local("doctor")
                    )
                ),
                selectModel = {},
                onFilterChanged = {},
            )
        }
    }
}
