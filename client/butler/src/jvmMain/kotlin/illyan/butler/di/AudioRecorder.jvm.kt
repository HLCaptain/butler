package illyan.butler.di

import illyan.butler.getAudioRecorder
import illyan.butler.utils.AudioRecorder
import org.koin.core.annotation.Single

@Single
actual fun provideAudioRecorder(): AudioRecorder {
    return getAudioRecorder()!!
}