package illyan.butler.core.local.room.model

import androidx.room.Entity

@Entity(
    tableName = "chats",
    primaryKeys = ["id"],
)
data class RoomChat(
    val id: String,
    val created: Long? = null,
    val name: String? = null,
    val ownerId: String,
    val chatCompletionModel: Pair<String, String>? = null, // URL to Model ID eg. https://api.openai.com/v1/ to gpt-4o
    val audioTranscriptionModel: Pair<String, String>? = null, // URL to Model ID eg. https://api.openai.com/v1/ to whisper-1
    val audioTranslationModel: Pair<String, String>? = null, // URL to Model ID eg. https://api.openai.com/v1/ to whisper-1
    val imageGenerationsModel: Pair<String, String>? = null, // URL to Model ID eg. https://api.openai.com/v1/ to dall-e-3
    val audioSpeechModel: Pair<String, String>? = null, // URL to Model ID eg. https://api.openai.com/v1/ to tts-1
    val summary: String? = null
)
