package illyan.butler.ui.chat_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.ButlerTooltipDefaults
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.RichTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.core.ui.utils.ReverseLayoutDirection
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.ErrorCode
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.assistant
import illyan.butler.generated.resources.message_id
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_messages
import illyan.butler.generated.resources.play
import illyan.butler.generated.resources.refresh_chat
import illyan.butler.generated.resources.select_chat
import illyan.butler.generated.resources.send
import illyan.butler.generated.resources.send_message
import illyan.butler.generated.resources.sender_id
import illyan.butler.generated.resources.stop
import illyan.butler.generated.resources.timestamp
import illyan.butler.generated.resources.you
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeApi::class)
@Composable
fun ChatDetail(
    state: ChatDetailState,
    sendMessage: (String) -> Unit,
    sendImage: (ByteArray, String, String) -> Unit,
    toggleRecord: (String) -> Unit,
    playAudio: (String) -> Unit,
    stopAudio: () -> Unit,
    refreshChat: () -> Unit,
    sendError: (ErrorCode) -> Unit,
    toggleChatDetails: () -> Unit,
    isChatDetailsOpenRatio: Float,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val hazeState = remember { HazeState() }
    var sentMessageButNoUpdate by remember { mutableStateOf(false) }
    val lastMessage = remember(state.messages) {
        (state.messages ?: emptyList()).maxByOrNull { it.time ?: 0L }
    }
    var sentMessageAndLoading by remember(lastMessage?.senderId, state.chat?.ownerId) {
        mutableStateOf(lastMessage?.senderId == state.chat?.ownerId)
    }
    LaunchedEffect(lastMessage?.senderId) {
        // Last message is sent by the user
        if (lastMessage?.senderId == state.chat?.ownerId ||
            // Or last message is from bot, but the message is blank
            (lastMessage?.senderId != state.chat?.ownerId && lastMessage?.messageContent?.isBlank() == true)) {
            delay(10000)
            sentMessageButNoUpdate = true
        } else {
            sentMessageButNoUpdate = false
        }
    }
    LaunchedEffect(sentMessageAndLoading) {
        if (sentMessageAndLoading) {
            delay(60000)
            sentMessageAndLoading = false
            sentMessageButNoUpdate = true
            sendError(ErrorCode.ChatRefreshError)
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.hazeEffect(hazeState) {
                    inputScale = HazeInputScale.None
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
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
                        IconButton(onClick = toggleChatDetails) {
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
            AnimatedVisibility(
                visible = state.chat != null,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ChatDetailBottomBar(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .imePadding()
                            .hazeEffect(hazeState)
                            .navigationBarsPadding(),
                        sendMessage = sendMessage,
                        sendImage = { content, type -> state.chat?.let { chat -> sendImage(content, type, chat.ownerId) } },
                        isRecording = state.isRecording,
                        toggleRecord = { state.chat?.let { toggleRecord(it.ownerId) } },
                    )
                }
            }
        },
    ) { innerPadding ->
        var widthOfBox by remember { mutableStateOf(0.dp) }
        Box(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState).layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                widthOfBox = placeable.width.toDp()
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                modifier = Modifier.hazeSource(hazeState),
                targetState = state.chat,
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200)) using SizeTransform(clip = false) { _, _ -> tween(0) }
                }
            ) { chat ->
                if (chat == null) {
                    SelectChat()
                } else {
                    AnimatedContent(
                        targetState = state.messages?.isEmpty() == true,
                        contentAlignment = Alignment.Center
                    ) { noMessages ->
                        if (noMessages) {
                            Text(
                                modifier = Modifier.padding(innerPadding),
                                text = stringResource(Res.string.no_messages),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        } else {
                            MessageList(
                                messages = state.messages ?: emptyList(),
                                preferedWidth = 640.dp,
                                userId = chat.ownerId,
                                sounds = state.sounds,
                                playAudio = playAudio,
                                playingAudio = state.playingAudio,
                                stopAudio = stopAudio,
                                images = state.images,
                                contentPadding = innerPadding,
                                sentMessageButNoUpdate = sentMessageButNoUpdate,
                                sentMessageAndLoading = sentMessageAndLoading,
                                refreshChat = {
                                    sentMessageButNoUpdate = false
                                    sentMessageAndLoading = true
                                    refreshChat()
                                }
                            )
                        }
                    }
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
    sendImage: (ByteArray, String) -> Unit,
    isRecording: Boolean = false,
    toggleRecord: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    preferedWidth: Dp,
    messages: List<DomainMessage> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    sounds: Map<String, Float> = emptyMap(), // Resource ID and length in seconds
    playAudio: (String) -> Unit = {},
    stopAudio: () -> Unit = {},
    refreshChat: () -> Unit = {},
    playingAudio: String? = null,
    images: Map<String, ByteArray> = emptyMap(), // URIs of images
    userId: String,
    sentMessageButNoUpdate: Boolean = false,
    sentMessageAndLoading: Boolean = false
) {
    val lazyListState = rememberLazyListState()
    LaunchedEffect(messages.isEmpty()) {
        lazyListState.animateScrollToItem(0)
    }
    LazyColumn(
        modifier = modifier.consumeWindowInsets(contentPadding),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = contentPadding,
        reverseLayout = true, // From bottom to up
    ) {
        if (sentMessageAndLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MediumCircularProgressIndicator()
                }
            }
        }
        items(messages.withIndex().sortedByDescending { it.value.time }, key = { it.value.id!! }) { (index, message) ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.widthIn(max = preferedWidth),
                    contentAlignment = if (message.senderId == userId) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column {
                        RichTooltipWithContent(
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
                            colors = ButlerTooltipDefaults.richTooltipColors
                        ) { gestureAreaModifier ->
                            MessageItem(
                                modifier = gestureAreaModifier.fillMaxWidth(),
                                message = message,
                                userId = userId,
                                sounds = sounds.filter { (key, _) -> message.resourceIds.contains(key) },
                                playAudio = playAudio,
                                playingAudio = playingAudio,
                                stopAudio = stopAudio,
                                images = images.filter { (key, _) -> message.resourceIds.contains(key) }.values.toList()
                            )
                        }
                        AnimatedVisibility(
                            visible = index == 0 && sentMessageButNoUpdate,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Spacer(modifier = Modifier.weight(1f))
                                    ButlerMediumTextButton(
                                        onClick = refreshChat,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = null
                                            )
                                        },
                                        text = { Text(text = stringResource(Res.string.refresh_chat)) }
                                    )
                                }
                            }
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
                    .sizeIn(maxHeight = 400.dp, maxWidth = if (sentByUser) 480.dp else 640.dp)
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
        if (!message.messageContent.isNullOrBlank()) {
            val cardColors = ButlerCardDefaults.cardColors(
                containerColor = if (sentByUser) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                contentColor = if (sentByUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            ReverseLayoutDirection(enabled = sentByUser) {
                ButlerCard(
                    modifier = Modifier.then(
                        if (sentByUser) Modifier.widthIn(max = 320.dp) else Modifier
                    ),
                    colors = cardColors,
                    contentPadding = ButlerCardDefaults.CompactContentPadding
                ) {
                    ReverseLayoutDirection(enabled = sentByUser) {
                        SelectionContainer {
                            // TODO: Use rich text state when code blocks are supported
//                            val richTextState = rememberRichTextState()
//                            LaunchedEffect(message.messageContent) {
//                                richTextState.setMarkdown(message.messageContent ?: "")
//                            }
//                            RichText(state = richTextState)
                            Markdown(
                                modifier = Modifier, // Don't use the default fillMaxSize here.
                                content = message.messageContent ?: "",
                                colors = markdownColor(
                                    text = MaterialTheme.colorScheme.onSurface,
                                    codeBackground = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                    inlineCodeBackground = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                    dividerColor = MaterialTheme.colorScheme.outline,
                                    tableBackground = MaterialTheme.colorScheme.surface,
                                ),
                                typography = markdownTypography()
                            )
                        }
                    }
                }
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

            ButlerMediumSolidButton(onClick = { if (isActive) onStop() else onPlay(resourceId) }) {
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
    sendImage: (ByteArray, String) -> Unit,
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
        val coroutineScope = rememberCoroutineScope()
        val launcher = rememberFilePickerLauncher(
            mode = FileKitMode.Single,
            type = FileKitType.Image
        ) { file ->
            file?.let {
                coroutineScope.launch {
                    sendImage(
                        file.readBytes(),
                        "image/${file.extension}"
                    )
                }
            }
        }
        AnimatedVisibility(visible = galleryEnabled) {
            IconButton(onClick = {
                if (galleryAccessGranted) {
                    launcher.launch()
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
        var textMessage by rememberSaveable { mutableStateOf("") }
        ButlerTextField(
            modifier = Modifier
                .weight(1f, fill = true)
                .padding(horizontal = 4.dp)
                .heightIn(max = 128.dp),
            value = textMessage,
            onValueChange = { textMessage = it },
            placeholder = { Text(stringResource(Res.string.send_message)) },
            singleLine = false,
            isCompact = true,
            isOutlined = false
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
