package illyan.butler.ui.chat_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerDropdownMenuBox
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.audio_speech_model
import illyan.butler.generated.resources.audio_transcription_model
import illyan.butler.generated.resources.audio_translation_model
import illyan.butler.generated.resources.chat_completion_model
import illyan.butler.generated.resources.chat_details
import illyan.butler.generated.resources.chat_id
import illyan.butler.generated.resources.image_generations_model
import illyan.butler.generated.resources.name
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.summary
import illyan.butler.generated.resources.unknown
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chatId: String?,
    actions: @Composable () -> Unit = {}
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
        setAudioTranscriptionModel = viewModel::setAudioTranscriptionModel,
        setAudioTranslationModel = viewModel::setAudioTranslationModel,
        setAudioSpeechModel = viewModel::setAudioSpeechModel,
        setImageGenerationsModel = viewModel::setImageGenerationsModel,
        actions = actions
    )
}

@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chat: DomainChat?,
    alternativeModels: List<Pair<String, String>>,
    setAudioTranscriptionModel: ((Pair<String, String>?) -> Unit)? = null,
    setAudioTranslationModel: ((Pair<String, String>?) -> Unit)? = null,
    setAudioSpeechModel: ((Pair<String, String>?) -> Unit)? = null,
    setImageGenerationsModel: ((Pair<String, String>?) -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Box {
        val hazeState = remember { HazeState() }
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent
        ) { innerPadding ->
            val aiMembers = remember(chat) {
                mapOf(
                    Res.string.chat_completion_model to chat?.chatCompletionModel,
                    Res.string.audio_speech_model to chat?.audioSpeechModel,
                    Res.string.audio_transcription_model to chat?.audioTranscriptionModel,
                    Res.string.audio_translation_model to chat?.audioTranslationModel,
                    Res.string.image_generations_model to chat?.imageGenerationsModel
                )
            }
            LazyColumn(
                modifier = Modifier.hazeSource(hazeState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = innerPadding + PaddingValues(8.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = stringResource(Res.string.chat_details),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                item {
                    SelectionContainer {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.name),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(chat?.name ?: stringResource(Res.string.new_chat))
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
                            Text(chat?.id ?: stringResource(Res.string.unknown))
                        }
                    }
                }
                items(aiMembers.toList(), key = { it.first.key }) { (resTitle, model) ->
                    ModelSetting(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(resTitle),
                        model = model,
                        enabled = resTitle != Res.string.chat_completion_model && alternativeModels.any { it != model },
                        alternatives = alternativeModels,
                        setModel = {
                            when (resTitle) {
                                Res.string.audio_transcription_model -> setAudioTranscriptionModel?.invoke(it)
                                Res.string.audio_translation_model -> setAudioTranslationModel?.invoke(it)
                                Res.string.audio_speech_model -> setAudioSpeechModel?.invoke(it)
                                Res.string.image_generations_model -> setImageGenerationsModel?.invoke(it)
                            }
                        }
                    )
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            actions()
        }
    }
}

@Composable
fun ModelSetting(
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = true,
    model: Pair<String, String>?,
    alternatives: List<Pair<String, String>>,
    setModel: ((Pair<String, String>?) -> Unit)? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ButlerDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        searchEnabled = true,
        onExpandedChange = { expanded = it },
        enabled = enabled,
        values = alternatives,
        selectValue = { expanded = false; setModel?.invoke(it) },
        selectedValue = model,
        settingName = title,
        trailingIcon = {
            IconButton(
                onClick = { expanded = !expanded },
                enabled = enabled
            ) {
                if (expanded) {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowUp, contentDescription = "Close dropdown")
                } else {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "Open dropdown")
                }
            }
        },
        leadingIcon = if (model != null && enabled) {
            {
                IconButton(
                    onClick = { setModel?.invoke(null) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null
                    )
                }
            }
        } else {
            null
        }
    )
}
