package illyan.butler.ui.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.assistant
import illyan.butler.generated.resources.back
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
import illyan.butler.ui.MediumCircularProgressIndicator
import illyan.butler.ui.chat_details.ChatDetailsScreen
import illyan.butler.ui.components.RichTooltipWithContent
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.home.LocalNavBarOrientation
import illyan.butler.ui.home.getNavBarTooltipGestures
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    viewModel: ChatDetailViewModel,
    currentSelectedChat: String?,
    canNavigateBack: Boolean = true,
    onNavigateBack: () -> Unit = {}
) {
    LaunchedEffect(currentSelectedChat) {
        currentSelectedChat?.let { viewModel.loadChat(it) }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var isChatDetailsDialogOpen by rememberSaveable { mutableStateOf(false) }
    val hazeState = remember { HazeState() }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (state.chat != null) {
                        IconButton(onClick = { isChatDetailsDialogOpen = true }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Chat details"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            // Bottom Padding is 8.dp for the TextField
            val navBarWindowInsets = WindowInsets(bottom = if (LocalNavBarOrientation.current == Orientation.Horizontal) 88.dp else 8.dp)
            Column(
                Modifier.consumeWindowInsets(WindowInsets.systemBars.union(navBarWindowInsets).only(WindowInsetsSides.Bottom)) // Height of Navigation Bar
            ) {
                if (state.chat?.id != null) {
                    MessageField(
                        modifier = Modifier.hazeChild(hazeState),
                        sendMessage = viewModel::sendMessage,
                        isRecording = state.isRecording,
                        canRecordAudio = state.canRecordAudio,
                        toggleRecord = viewModel::toggleRecording,
                        sendImage = viewModel::sendImage,
                        galleryAccessGranted = state.galleryPermission == PermissionStatus.Granted,
                        requestGalleryAccess = viewModel::requestGalleryPermission
                    )
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.haze(hazeState, HazeMaterials.thin()),
        ) {
            AnimatedVisibility(state.chat?.id == null) {
                SelectChat()
            }
            if (state.chat?.id != null) {
                MessageList(
                    modifier = Modifier.weight(1f, fill = true),
                    messages = state.messages ?: emptyList(),
                    userId = state.userId ?: "",
                    sounds = state.sounds,
                    playAudio = viewModel::playAudio,
                    playingAudio = state.playingAudio,
                    stopAudio = viewModel::stopAudio,
                    images = state.images,
                    innerPadding = innerPadding
                )
            }
        }
    }
    ButlerDialog(
        isDialogOpen = isChatDetailsDialogOpen,
        onDismissDialog = { isChatDetailsDialogOpen = false },
    ) {
        ChatDetailsScreen(state.chat, state.userId)
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (messages.isEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(innerPadding),
                text = stringResource(Res.string.no_messages),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        LazyColumn(
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = innerPadding
        ) {
            items(messages) { message ->
                RichTooltipWithContent(
                    enabledGestures = getNavBarTooltipGestures(),
                    tooltip = {
                        val keyValueList = listOf(
                            Res.string.message_id to message.id,
                            Res.string.timestamp to message.time?.let {
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .format(LocalDateTime.Formats.ISO)
                            },
                            Res.string.sender_id to message.senderId,
                        ).filter { it.second != null }
                        LazyColumn {
                            items(keyValueList, key = { it.first }) {
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
    LaunchedEffect(images) {
        Napier.d("Number of images for message ${message.id}: ${images.size}")
    }
    val sentByUser = message.senderId == userId
    LaunchedEffect(sentByUser) {
        Napier.d("Message ${message.id} sent by user: $sentByUser, senderId: ${message.senderId}, userId: $userId")
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                imageLoader = ImageLoader(LocalPlatformContext.current),
                contentDescription = "Image"
            ) {
                when (val state = painter.state) {
                    AsyncImagePainter.State.Empty -> Text("Empty")
                    is AsyncImagePainter.State.Error -> {
                        Text("Error loading image")
                        Napier.e("Error loading image with size ${image.size} and painter.state: ${painter.state}", state.result.throwable)
                    }
                    is AsyncImagePainter.State.Loading -> MediumCircularProgressIndicator()
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                }
            }
        }
        if (!message.message.isNullOrBlank()) {
            val cardColors = if (sentByUser) CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) else CardDefaults.elevatedCardColors()
            ElevatedCard(
                colors = cardColors
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = message.message
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

            Button(onClick = { if (isActive) onStop() else onPlay(resourceId) },) {
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageField(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit,
    isRecording: Boolean = false,
    canRecordAudio: Boolean = false,
    toggleRecord: () -> Unit,
    sendImage: (String) -> Unit,
    galleryAccessGranted: Boolean = false,
    requestGalleryAccess: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = canRecordAudio) {
            IconButton(onClick = toggleRecord) {
                Crossfade(isRecording) {
                    if (it) {
                        Icon(
                            imageVector = Icons.Rounded.StopCircle,
                            contentDescription = stringResource(Res.string.send),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = stringResource(Res.string.send),
                            tint =  MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        var isFilePickerShown by rememberSaveable { mutableStateOf(false) }
        IconButton(onClick = {
            if (galleryAccessGranted) {
                isFilePickerShown = true
            } else {
                requestGalleryAccess()
            }
        }) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = stringResource(Res.string.send),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        FilePicker(
            show = isFilePickerShown,
            fileExtensions = listOf("jpg", "jpeg", "png"),
        ) { platformFile ->
            isFilePickerShown = false
            platformFile?.path?.let(sendImage)
        }
        var textMessage by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f, fill = true),
            value = textMessage,
            onValueChange = { textMessage = it },
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(Res.string.send_message)) }
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