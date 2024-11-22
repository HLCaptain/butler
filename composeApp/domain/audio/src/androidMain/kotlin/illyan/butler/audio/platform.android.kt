package illyan.butler.audio

import org.koin.core.context.GlobalContext

actual fun getAudioRecorder(): AudioRecorder? {
    return AndroidAudioRecorder(GlobalContext.get().get())
}
