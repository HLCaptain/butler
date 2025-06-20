@file:OptIn(ExperimentalUuidApi::class)

package illyan.butler.ui.chat_details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerDropdownMenuBox
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.Chat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.audio_speech_model
import illyan.butler.generated.resources.audio_transcription_model
import illyan.butler.generated.resources.audio_translation_model
import illyan.butler.generated.resources.chat_completion_model
import illyan.butler.generated.resources.chat_details
import illyan.butler.generated.resources.chat_details_model_config
import illyan.butler.generated.resources.chat_id
import illyan.butler.generated.resources.image_generations_model
import illyan.butler.generated.resources.name
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.summary
import illyan.butler.generated.resources.unknown
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chatId: Uuid?,
    actions: @Composable RowScope.() -> Unit = {},
    hazeState: HazeState = remember { HazeState() },
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val viewModel = koinViewModel<ChatDetailsViewModel>()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(chatId) {
        chatId?.let { viewModel.loadChat(it) }
    }
    ChatDetails(
        modifier = modifier,
        chat = state.chat,
        alternativeModels = state.alternativeModels,
        setModel = viewModel::setModel,
        hazeState = hazeState,
        actions = actions,
        navigationIcon = navigationIcon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chat: Chat?,
    alternativeModels: List<AiSource>,
    setModel: ((AiSource?, Capability) -> Unit)? = null,
    hazeState: HazeState = remember { HazeState() },
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null
) {
    Scaffold(
        modifier = modifier.hazeEffect(hazeState),
        topBar = {
            TopAppBar(
                modifier = Modifier.hazeEffect(hazeState),
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = stringResource(Res.string.chat_details),
                    )
                },
                navigationIcon = navigationIcon ?: {},
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                actions = actions
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        val aiMembers = remember(chat) {
            mapOf(
                Capability.CHAT_COMPLETION to chat?.models[Capability.CHAT_COMPLETION],
                Capability.SPEECH_SYNTHESIS to chat?.models[Capability.SPEECH_SYNTHESIS],
                Capability.AUDIO_TRANSCRIPTION to chat?.models[Capability.AUDIO_TRANSCRIPTION],
                Capability.AUDIO_TRANSLATION to chat?.models[Capability.AUDIO_TRANSLATION],
                Capability.IMAGE_GENERATION to chat?.models[Capability.IMAGE_GENERATION]
            )
        }
        LazyColumn(
            modifier = Modifier.hazeSource(hazeState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = innerPadding + PaddingValues(8.dp)
        ) {
            item {
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.name),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(chat?.title ?: stringResource(Res.string.new_chat))
                    }
                }
            }
            item {
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.summary),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(chat?.summary ?: stringResource(Res.string.unknown))
                    }
                }
            }
            item {
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.chat_id),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(chat?.id?.toString() ?: stringResource(Res.string.unknown))
                    }
                }
            }
            items(aiMembers.toList(), key = { (capability, _) -> capability }) { (capability, model) ->
                ModelSetting(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(
                        when (capability) {
                            Capability.CHAT_COMPLETION -> Res.string.chat_completion_model
                            Capability.AUDIO_TRANSCRIPTION -> Res.string.audio_transcription_model
                            Capability.AUDIO_TRANSLATION -> Res.string.audio_translation_model
                            Capability.SPEECH_SYNTHESIS -> Res.string.audio_speech_model
                            Capability.IMAGE_GENERATION -> Res.string.image_generations_model
                        }
                    ),
                    model = model,
                    enabled = capability != Capability.CHAT_COMPLETION && alternativeModels.any { it != model },
                    alternatives = alternativeModels,
                    setModel = { setModel?.invoke(it, capability) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSetting(
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = true,
    model: AiSource?,
    alternatives: List<AiSource>,
    setModel: ((AiSource?) -> Unit)? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ButlerDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        searchEnabled = true,
        onExpandedChange = { expanded = it },
        enabled = enabled,
        values = alternatives,
        getValueName = {
            stringResource(
                Res.string.chat_details_model_config,
                it.modelId,
                it.endpoint
            )
        },
        item = {
            Column {
                Text(text = it.modelId)
                Text(
                    text = it.endpoint,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light
                    ),
                )
            }
        },
        selectValue = { expanded = false; setModel?.invoke(it) },
        selectedValue = model,
        settingName = title,
        trailingIcon = {
            val rotation by animateFloatAsState(if (expanded) 180f else 0f)
            Icon(
                modifier = Modifier.graphicsLayer { rotationZ = rotation },
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Dropdown expended icon"
            )
        },
        leadingIcon = if (model != null && enabled) { {
            IconButton(onClick = { setModel?.invoke(null) }) {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = null
                )
            }
        } } else {
            null
        }
    )
}
