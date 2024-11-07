package illyan.butler.audio

expect fun getAudioRecorder(): AudioRecorder?

fun canRecordAudio() = getAudioRecorder() != null
