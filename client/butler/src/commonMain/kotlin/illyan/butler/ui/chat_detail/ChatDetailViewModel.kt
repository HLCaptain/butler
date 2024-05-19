package illyan.butler.ui.chat_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.DomainResource
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.manager.AudioManager
import illyan.butler.manager.AuthManager
import illyan.butler.manager.ChatManager
import illyan.butler.manager.PermissionManager
import illyan.butler.utils.toWav
import io.github.aakira.napier.Napier
import korlibs.audio.format.MP3
import korlibs.audio.sound.AudioData
import korlibs.time.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatManager: ChatManager,
    private val authManager: AuthManager,
    private val audioManager: AudioManager,
    private val permissionManager: PermissionManager,
) : ViewModel() {
    private val chatIdStateFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val chat = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getChatFlow(chatId) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = chatIdStateFlow
        .flatMapLatest { chatId -> chatId?.let { chatManager.getMessagesByChatFlow(chatId) } ?: flowOf(emptyList()) }
        .map { messages -> messages.sortedBy { it.time }.reversed() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resources = messages.flatMapLatest { messages ->
        combine((messages).map { message ->
            chatManager.getResourcesByMessageFlow(message.id!!)
        }) { flows ->
            val resources = flows.toList().filterNotNull().flatten()
//            Napier.d("Resources: ${resources.map { resource -> resource?.id }}")
            resources
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val state = combine(
        chat,
        messages,
        authManager.signedInUserId,
        audioManager.isRecording,
        audioManager.playingAudioId,
        resources,
        permissionManager.getPermissionStatus(Permission.GALLERY)
    ) { flows ->
        val chat = flows[0] as? DomainChat
        val messages = flows[1] as? List<DomainMessage>
        val userId = flows[2] as? String
//        Napier.v { "User ID: $userId" }
        val recording = flows[3] as? Boolean ?: false
        val playing = flows[4] as? String
        val resources = flows[5] as? List<DomainResource>
        val galleryPermission = flows[6] as? PermissionStatus
//        Napier.v("Gallery permission: $galleryPermission")
//        Napier.v("Resources: $resources")
        val sounds = resources?.filter { it.type.startsWith("audio") }
            ?.associate { it.id!! to it.data.toAudioData(it.type)!!.totalTime.seconds.toFloat() } ?: emptyMap()
        val images = resources?.filter { it.type.startsWith("image") }
            ?.associate { it.id!! to it.data } ?: emptyMap()
        ChatDetailState(
            chat = chat,
            messages = messages,
            userId = userId,
            isRecording = recording,
            canRecordAudio = audioManager.canRecordAudio,
            playingAudio = playing,
            sounds = sounds,
            images = images,
            galleryPermission = galleryPermission
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ChatDetailState(canRecordAudio = audioManager.canRecordAudio)
    )

    fun loadChat(chatId: String) {
        chatIdStateFlow.update { chatId }
    }

    val userId = authManager.signedInUserId

    fun sendMessage(message: String) {
        viewModelScope.launch {
            chat.value?.id?.let { chatManager.sendMessage(it, message) }
        }
    }

    fun toggleRecording() {
        if (!audioManager.canRecordAudio) return
        viewModelScope.launch {
            if (state.value.isRecording) {
                val audioId = audioManager.stopRecording()
                chatIdStateFlow.value?.let { chatManager.sendAudioMessage(it, audioId) }
            } else {
                audioManager.startRecording()
            }
        }
    }

    fun sendImage(path: String) {
        viewModelScope.launch {
            chatIdStateFlow.value?.let {
                chatManager.sendImageMessage(it, path)
                Napier.d("Image sent: $path")
            }
        }
    }

    fun playAudio(audioId: String) {
        viewModelScope.launch {
            audioManager.playAudio(audioId)
        }
    }

    fun stopAudio() {
        viewModelScope.launch {
            audioManager.stopAudio()
        }
    }

    fun requestGalleryPermission() {
        viewModelScope.launch {
            permissionManager.preparePermissionRequest(Permission.GALLERY)
        }
    }
}

private suspend fun ByteArray.toAudioData(mimeType: String): AudioData? {
    return when (mimeType) {
        "audio/wav" -> toWav()
        "audio/mp3" -> MP3.decode(this)
        else -> null
    }
}