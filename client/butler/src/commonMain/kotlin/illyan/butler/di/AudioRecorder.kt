package illyan.butler.di

import illyan.butler.utils.audio.AudioRecorder
import org.koin.core.annotation.Single

@Single
expect fun provideAudioRecorder(): AudioRecorder