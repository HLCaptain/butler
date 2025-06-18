@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.ui.chat_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerButtonDefaults
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.ButlerOutlinedCard
import illyan.butler.core.ui.components.ButlerSmallTextButton
import illyan.butler.core.ui.components.ButlerTag
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.ButlerTooltipDefaults
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.components.RichTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.core.ui.utils.ReverseLayoutDirection
import illyan.butler.domain.model.ErrorCode
import illyan.butler.domain.model.Message
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.assistant
import illyan.butler.generated.resources.free
import illyan.butler.generated.resources.host
import illyan.butler.generated.resources.message_id
import illyan.butler.generated.resources.model_id
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_messages
import illyan.butler.generated.resources.play
import illyan.butler.generated.resources.refresh_chat
import illyan.butler.generated.resources.select_model
import illyan.butler.generated.resources.selected_model
import illyan.butler.generated.resources.send
import illyan.butler.generated.resources.send_message
import illyan.butler.generated.resources.sender_id
import illyan.butler.generated.resources.stop
import illyan.butler.generated.resources.system
import illyan.butler.generated.resources.system_prompt
import illyan.butler.generated.resources.timestamp
import illyan.butler.generated.resources.you
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun RefreshMessageEffect(
    lastMessage: Message?,
    noUpdateDelay: Duration,
    sentMessageAndLoading: Boolean,
    onSentMessageAndLoading: (Boolean) -> Unit,
    onMessageUpdated: (Boolean) -> Unit,
    refreshErrorDelay: Duration,
    onRefreshError: () -> Unit
) {
    LaunchedEffect(lastMessage?.sender) {
        // Last message is sent by the user
        if (lastMessage?.sender is SenderType.User ||
            // Or last message is from bot, but the message is blank
            lastMessage?.content?.isBlank() == true) {
            delay(noUpdateDelay)
            onMessageUpdated(true)
        } else {
            onMessageUpdated(false)
        }
    }
    LaunchedEffect(sentMessageAndLoading) {
        if (sentMessageAndLoading) {
            delay(refreshErrorDelay)
            onSentMessageAndLoading(false)
            onMessageUpdated(true)
            onRefreshError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeApi::class, ExperimentalUuidApi::class)
@Composable
fun ChatDetail(
    state: ChatDetailState,
    sendMessage: (String) -> Unit,
    sendImage: (ByteArray, String) -> Unit,
    toggleRecord: (Source) -> Unit,
    playAudio: (Uuid) -> Unit,
    stopAudio: () -> Unit,
    refreshChat: () -> Unit,
    sendError: (ErrorCode) -> Unit,
    navigateToModelSelection: () -> Unit,
    navigateToChatSettings: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val hazeState = remember { HazeState() }
    var sentMessageButNoUpdate by remember { mutableStateOf(false) }
    val lastMessage = remember(state.messages) { state.messages?.maxByOrNull { it.createdAt.toEpochMilliseconds() } }
    var sentMessageAndLoading by remember(lastMessage?.sender) {
        mutableStateOf(lastMessage?.sender is SenderType.User)
    }
    val lazyListState = rememberLazyListState()
    RefreshMessageEffect(
        lastMessage = lastMessage,
        noUpdateDelay = 10.seconds,
        sentMessageAndLoading = sentMessageAndLoading,
        onSentMessageAndLoading = { sentMessageAndLoading = it },
        onMessageUpdated = { sentMessageButNoUpdate = it },
        refreshErrorDelay = 20.seconds,
        onRefreshError = { sendError(ErrorCode.ChatRefreshError) }
    )
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
                        state.chat?.title ?: stringResource(Res.string.new_chat),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = navigationIcon ?: {},
                scrollBehavior = scrollBehavior,
                actions = {
                    AnimatedVisibility(
                        visible = state.chat != null,
                        enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut(tween(200)) + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        IconButton(
                            onClick = navigateToChatSettings,
                            enabled = state.chat != null
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ChatDetailBottomBar(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .imePadding()
                        .hazeEffect(hazeState)
                        .navigationBarsPadding(),
                    sendMessage = { content -> sendMessage(content) },
                    sendImage = { content, type -> sendImage(content, type) },
                    isRecording = state.isRecording,
                    toggleRecord = { state.chat?.let { toggleRecord(it.source) } },
                    enabled = state.chat != null || state.selectedNewChatModel != null,
                    currentModel = state.selectedNewChatModel
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            var isScrollingToStart by remember { mutableStateOf(false) }
            LaunchedEffect(lazyListState.isScrollInProgress) {
                if (!lazyListState.isScrollInProgress && isScrollingToStart) {
                    isScrollingToStart = false
                }
            }
            val fabVisible by remember {
                derivedStateOf {
                    val notAtStart = lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
                    notAtStart && when {
                        lazyListState.isScrollInProgress -> !isScrollingToStart
                        else -> true
                    }
                }
            }
            val coroutineScope = rememberCoroutineScope()
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn(tween(200)) + slideInVertically { it },
                exit = fadeOut(tween(200)) + slideOutVertically { it }
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                            isScrollingToStart = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
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
                    StartChatModelInfo(
                        modifier = Modifier.padding(innerPadding),
                        navigateToModelSelection = navigateToModelSelection,
                        selectedModel = state.selectedNewChatModel
                    )
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
                                preferredWidth = 640.dp,
                                sounds = state.sounds,
                                playAudio = playAudio,
                                playingAudio = state.playingAudio,
                                stopAudio = stopAudio,
                                images = state.images,
                                contentPadding = innerPadding,
                                lazyListState = lazyListState,
                                sentMessageButNoUpdate = sentMessageButNoUpdate,
                                sentMessageAndLoading = sentMessageAndLoading,
                                refreshChat = {
                                    sentMessageButNoUpdate = false
                                    sentMessageAndLoading = true
                                    refreshChat()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartChatModelInfo(
    modifier: Modifier = Modifier,
    selectedModel: AiSource? = null,
    navigateToModelSelection: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedModel != null) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(Res.string.selected_model),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                ModelInfo(
                    aiSource = selectedModel,
                    onClick = navigateToModelSelection
                )
            } else {
                ButlerLargeSolidButton(
                    onClick = navigateToModelSelection,
                ) {
                    Text(stringResource(Res.string.select_model))
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelInfo(
    modifier: Modifier = Modifier,
    aiSource: AiSource,
    onClick: () -> Unit
) {
    ButlerOutlinedCard(
        modifier = modifier,
        contentPadding = ButlerCardDefaults.CompactContentPadding,
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                PlainTooltipWithContent(
                    onClick = onClick,
                    tooltip = { Text(aiSource.modelId) }
                ) { tooltipModifier ->
                    val modelIdWithoutCompany = aiSource.modelId.replace(":free", "").substringAfter('/')
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f, fill = false).clip(RoundedCornerShape(6.dp))) {
                            Text(
                                modifier = tooltipModifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                text = AiSource.getNameFromId(modelIdWithoutCompany),
                                maxLines = 2,
                                style = MaterialTheme.typography.titleLarge,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (aiSource.modelId.contains("free", ignoreCase = true)) {
                            ButlerTag {
                                Text(text = stringResource(Res.string.free))
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.model_id),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = aiSource.modelId,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.host),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = aiSource.endpoint,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(Res.string.select_model),
                )
            }
        }
    }
}

@Composable
expect fun ChatDetailBottomBar(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (ByteArray, String) -> Unit,
    isRecording: Boolean = false,
    toggleRecord: () -> Unit,
    enabled: Boolean = true,
    currentModel: AiSource? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    preferredWidth: Dp,
    messages: List<Message> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    sounds: Map<Uuid, Float> = emptyMap(), // Resource ID and length in seconds
    playAudio: (Uuid) -> Unit = {},
    stopAudio: () -> Unit = {},
    refreshChat: () -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
    playingAudio: Uuid? = null,
    images: Map<Uuid, ByteArray> = emptyMap(), // URIs of images
    sentMessageButNoUpdate: Boolean = false,
    sentMessageAndLoading: Boolean = false
) {
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
        items(messages.withIndex().sortedByDescending { it.value.createdAt }, key = { it.value.id }) { (index, message) ->
            Box(
                modifier = Modifier.animateItem().fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.widthIn(max = preferredWidth),
                    contentAlignment = if (message.sender is SenderType.User) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column {
                        val systemString = stringResource(Res.string.system)
                        RichTooltipWithContent(
                            enabledGestures = getTooltipGestures(),
                            tooltip = {
                                val keyValueList = remember(message) {
                                    listOf(
                                        Res.string.message_id to message.id.toString(),
                                        Res.string.timestamp to message.createdAt.toString(),
                                        Res.string.sender_id to when (val sender = message.sender) {
                                            is SenderType.User -> when (val source = sender.source) {
                                                is Source.Server -> source.userId
                                                is Source.Device -> source.deviceId
                                            }.toString()
                                            is SenderType.Ai -> sender.source.modelId
                                            is SenderType.System -> systemString
                                        },
                                    )
                                }
                                Column {
                                    keyValueList.forEach {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(text = stringResource(it.first))
                                            Text(text = it.second)
                                        }
                                    }
                                }
                            },
                            colors = ButlerTooltipDefaults.richTooltipColors
                        ) { gestureAreaModifier ->
                            MessageItem(
                                modifier = gestureAreaModifier.fillMaxWidth(),
                                message = message,
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
fun SystemMessageItem(
    modifier: Modifier = Modifier,
    message: Message,
) {
    var expanded by remember { mutableStateOf(false) }
    ButlerOutlinedCard(
        modifier = modifier.padding(8.dp),
        contentPadding = ButlerCardDefaults.CompactContentPadding,
        onClick = { expanded = !expanded },
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerSmallTextButton(
                onClick = { expanded = !expanded },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null
                    )
                },
                colors = ButlerButtonDefaults.textButtonGrayColors()
            ) {
                Text(
                    text = stringResource(Res.string.system_prompt),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                SelectionContainer {
                    Text(
                        text = message.content ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message,
    sounds: Map<Uuid, Float> = emptyMap(), // Resource ID and length in seconds
    playAudio: (Uuid) -> Unit = {},
    stopAudio: () -> Unit = {},
    playingAudio: Uuid? = null,
    images: List<ByteArray> = emptyList(),
) {

    val sentByUser = remember(message.sender) { message.sender is SenderType.User }
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = if (sentByUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = stringResource(
                when (message.sender) {
                    is SenderType.User -> Res.string.you
                    is SenderType.Ai -> Res.string.assistant
                    is SenderType.System -> Res.string.system
                },
            ),
            style = MaterialTheme.typography.labelMedium
        )
        if (message.sender == SenderType.System) {
            SystemMessageItem(
                modifier = Modifier.fillMaxWidth(),
                message = message
            )
        } else {
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
            if (!message.content.isNullOrBlank()) {
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
                                var validMarkdownContent by remember(message.content) { mutableStateOf(message.content) }
                                LaunchedEffect(message.content) {
                                    // Validate markdown content to avoid rendering issues
                                    validMarkdownContent = message.content?.takeIf { it.isNotBlank() } ?: validMarkdownContent
                                }
                                val markdownState = rememberMarkdownState(validMarkdownContent ?: "")
                                Markdown(
                                    modifier = Modifier, // Don't use the default fillMaxSize here.
                                    markdownState = markdownState,
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
}

@Composable
fun AudioMessages(
    resources: Map<Uuid, Float>, // Audio to length in seconds
    onPlay: (Uuid) -> Unit = {},
    onStop: () -> Unit = {},
    isPlaying: Uuid? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        resources.forEach { (resourceId, length) ->
            var progress by remember { mutableFloatStateOf(0f) }
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
    requestRecordAudioAccess: () -> Unit,
    enabled: Boolean = true,
    currentModel: AiSource? = null
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = recordAudioEnabled) {
            IconButton(
                onClick = {
                    if (recordAudioAccessGranted) {
                        toggleRecord()
                    } else {
                        requestRecordAudioAccess()
                    }
                },
                enabled = enabled
            ) {
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
            IconButton(
                onClick = {
                    if (galleryAccessGranted) {
                        launcher.launch()
                    } else {
                        requestGalleryAccess()
                    }
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        var textMessage by rememberSaveable { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        ButlerTextField(
            modifier = Modifier
                .weight(1f, fill = true)
                .padding(horizontal = 4.dp)
                .heightIn(max = 128.dp)
                .focusRequester(focusRequester),
            value = textMessage,
            onValueChange = { textMessage = it },
            placeholder = { Text(stringResource(Res.string.send_message)) },
            singleLine = false,
            isCompact = true,
            isOutlined = false,
            enabled = enabled
        )

        LaunchedEffect(currentModel) {
            if (enabled && currentModel != null) {
                focusRequester.requestFocus()
            }
        }

        IconButton(
            onClick = {
                sendMessage(textMessage)
                textMessage = ""
            },
            enabled = enabled && textMessage.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.send),
                tint =  MaterialTheme.colorScheme.primary
            )
        }
    }
}
