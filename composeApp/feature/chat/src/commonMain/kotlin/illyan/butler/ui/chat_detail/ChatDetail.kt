package illyan.butler.ui.chat_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.RichTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.domain.model.DomainMessage
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.assistant
import illyan.butler.generated.resources.message_id
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_messages
import illyan.butler.generated.resources.play
import illyan.butler.generated.resources.select_chat
import illyan.butler.generated.resources.send
import illyan.butler.generated.resources.send_message
import illyan.butler.generated.resources.sender_id
import illyan.butler.generated.resources.stop
import illyan.butler.generated.resources.timestamp
import illyan.butler.generated.resources.you
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetail(
    state: ChatDetailState,
    sendMessage: (String) -> Unit,
    sendImage: (String) -> Unit,
    toggleRecord: () -> Unit,
    playAudio: (String) -> Unit,
    stopAudio: () -> Unit,
    openChatDetails: () -> Unit,
    isChatDetailsOpenRatio: Float,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val hazeState = remember { HazeState() }
    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .clip(
                    RoundedCornerShape(
                        topEnd = 24.dp * isChatDetailsOpenRatio,
                        bottomEnd = 24.dp * isChatDetailsOpenRatio
                    )
                ),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.hazeChild(hazeState),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent),
                    title = {
                        Text(
                            state.chat?.name ?: stringResource(Res.string.new_chat),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = navigationIcon ?: {},
                    actions = {
                        if (state.chat != null) {
                            IconButton(onClick = openChatDetails) {
                                val layoutDirection = LocalLayoutDirection.current
                                val imageVector = if (layoutDirection == LayoutDirection.Ltr) {
                                    if (isChatDetailsOpenRatio > 0.5f) Icons.Rounded.KeyboardDoubleArrowRight else Icons.Rounded.KeyboardDoubleArrowLeft
                                } else {
                                    if (isChatDetailsOpenRatio > 0.5f) Icons.Rounded.KeyboardDoubleArrowLeft else Icons.Rounded.KeyboardDoubleArrowRight
                                }
                                Icon(
                                    imageVector = imageVector,
                                    contentDescription = "Chat details"
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                var isFilePickerShown by rememberSaveable { mutableStateOf(false) }
                if (state.chat?.id != null) {
                    ChatDetailBottomBar(
                        modifier = Modifier.imePadding().hazeChild(hazeState).navigationBarsPadding(),
                        sendMessage = sendMessage,
                        sendImage = sendImage,
                        isRecording = state.isRecording,
                        toggleRecord = toggleRecord,
                        isFilePickerShown = isFilePickerShown,
                        showFilePicker = { isFilePickerShown = it }
                    )
                }
            },
        ) { innerPadding ->
            Column(modifier = Modifier.haze(hazeState)) {
                AnimatedVisibility(state.chat?.id == null) {
                    SelectChat()
                }
                if (state.chat?.id != null) {
                    MessageList(
                        modifier = Modifier.weight(1f, fill = true),
                        messages = state.messages ?: emptyList(),
                        userId = state.userId ?: "",
                        sounds = state.sounds,
                        playAudio = playAudio,
                        playingAudio = state.playingAudio,
                        stopAudio = stopAudio,
                        images = state.images,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectChat() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.select_chat))
    }
}

@Composable
expect fun ChatDetailBottomBar(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (String) -> Unit,
    isRecording: Boolean = false,
    toggleRecord: () -> Unit,
    isFilePickerShown: Boolean,
    showFilePicker: (Boolean) -> Unit
)

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<DomainMessage> = emptyList(),
    sounds: Map<String, Float> = emptyMap(), // Resource ID and length in seconds
    playAudio: (String) -> Unit = {},
    stopAudio: () -> Unit = {},
    playingAudio: String? = null,
    images: Map<String, ByteArray> = emptyMap(), // URIs of images
    userId: String,
    innerPadding: PaddingValues
) {
    AnimatedContent(
        modifier = modifier,
        targetState = messages.isEmpty()
    ) { noMessages ->
        if (noMessages) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_messages),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = innerPadding,
                reverseLayout = true // From bottom to up
            ) {
                items(messages.sortedByDescending { it.time }, key = { it.id!! }) { message ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (message.senderId == userId) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        RichTooltipWithContent(
                            modifier = Modifier.animateItem(),
                            enabledGestures = getTooltipGestures(),
                            tooltip = {
                                val keyValueList = remember(message) {
                                    listOf(
                                        Res.string.message_id to message.id,
                                        Res.string.timestamp to message.time?.let {
                                            Instant.fromEpochMilliseconds(it)
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .format(LocalDateTime.Formats.ISO)
                                        },
                                        Res.string.sender_id to message.senderId,
                                    ).filter { it.second != null }
                                }
                                Column {
                                    keyValueList.forEach {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(text = stringResource(it.first))
                                            Text(text = it.second!!)
                                        }
                                    }
                                }
                            },
                        ) { gestureAreaModifier ->
                            MessageItem(
                                modifier = gestureAreaModifier,
                                message = message,
                                userId = userId,
                                sounds = sounds.filter { (key, _) -> message.resourceIds.contains(key) },
                                playAudio = playAudio,
                                playingAudio = playingAudio,
                                stopAudio = stopAudio,
                                images = images.filter { (key, _) -> message.resourceIds.contains(key) }.values.toList()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: DomainMessage,
    sounds: Map<String, Float> = emptyMap(), // Resource ID and length in seconds
    playAudio: (String) -> Unit = {},
    stopAudio: () -> Unit = {},
    playingAudio: String? = null,
    images: List<ByteArray> = emptyList(),
    userId: String
) {
    val sentByUser = remember(message, userId) { message.senderId == userId }
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = if (sentByUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = stringResource(if (sentByUser) Res.string.you else Res.string.assistant),
            style = MaterialTheme.typography.labelMedium
        )
        if (sounds.isNotEmpty()) {
            AudioMessages(
                resources = sounds,
                onPlay = playAudio,
                onStop = stopAudio,
                isPlaying = playingAudio
            )
        }
        images.forEach { image ->
            SubcomposeAsyncImage(
                modifier = Modifier
                    .sizeIn(maxHeight = 400.dp, maxWidth = 400.dp)
                    .clip(RoundedCornerShape(8.dp)),
                model = image,
                loading = { MediumCircularProgressIndicator() },
                error = { Text("Error loading image") },
                success = { _ ->
                    SubcomposeAsyncImageContent()
                },
                contentDescription = "Image"
            )
        }
        if (!message.message.isNullOrBlank()) {
            val cardColors = CardDefaults.cardColors(
                containerColor = if (sentByUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
            Card(colors = cardColors) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = message.message ?: ""
                )
            }
        }
    }
}

@Composable
fun AudioMessages(
    resources: Map<String, Float>, // Audio to length in seconds
    onPlay: (String) -> Unit = {},
    onStop: () -> Unit = {},
    isPlaying: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        resources.forEach { (resourceId, length) ->
            var progress by remember { mutableStateOf(0f) }
            val isActive = isPlaying == resourceId

            LaunchedEffect(isActive, progress) {
                if (isActive && progress < 1f) {
                    delay(100)
                    progress += 0.1f / length
                    if (progress >= 1f) {
                        onStop()
                        progress = 0f
                    }
                } else if (!isActive && progress > 0f) progress = 0f
            }

            Button(onClick = { if (isActive) onStop() else onPlay(resourceId) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (isActive) Res.string.stop else Res.string.play),
                    )
                    Text(
                        text = stringResource(if (isActive) Res.string.stop else Res.string.play),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageField(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit,
    isRecording: Boolean = false,
    toggleRecord: () -> Unit,
    isFilePickerShown: Boolean,
    setFilePickerShown: (Boolean) -> Unit,
    sendImage: (String) -> Unit,
    galleryAccessGranted: Boolean = false,
    galleryEnabled: Boolean = false,
    recordAudioAccessGranted: Boolean = false,
    recordAudioEnabled: Boolean = false,
    requestGalleryAccess: () -> Unit,
    requestRecordAudioAccess: () -> Unit
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = recordAudioEnabled) {
            IconButton(onClick = {
                if (recordAudioAccessGranted) {
                    toggleRecord()
                } else {
                    requestRecordAudioAccess()
                }
            }) {
                Crossfade(isRecording) {
                    if (it) {
                        Icon(
                            imageVector = Icons.Rounded.StopCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = null,
                            tint =  MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = galleryEnabled) {
            IconButton(onClick = {
                if (galleryAccessGranted) {
                    setFilePickerShown(true)
                } else {
                    requestGalleryAccess()
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        FilePicker(
            show = isFilePickerShown,
            fileExtensions = listOf("jpg", "jpeg", "png"),
        ) { platformFile ->
            setFilePickerShown(false)
            platformFile?.path?.let(sendImage)
        }
        var textMessage by rememberSaveable { mutableStateOf("") }
        ButlerTextField(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f, fill = true),
            value = textMessage,
            onValueChange = { textMessage = it },
            placeholder = { Text(stringResource(Res.string.send_message)) },
            maxLines = 1
        )

        IconButton(
            onClick = {
                sendMessage(textMessage)
                textMessage = ""
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.send),
                tint =  MaterialTheme.colorScheme.primary
            )
        }
    }
}