package illyan.butler.domain.model

data class DomainModel(
    val name: String?,
    val id: String,
    val endpoint: String,
    val ownedBy: String?
) {
    companion object {
        fun getNameFromId(id: String): String {
            // Get recommended name from the ID of a model
            // Like
            // "gpt-o3" -> "GPT-o3"
            // "tts-2.5" -> "TTS-2.5"
            // "Whisper-2" -> "Whisper 2"
            // "gpt-6o" -> "GPT-6o"
            // "gpt-5o-mini" -> "GPT-5o Mini"
            // "Llama-3" -> "Llama 3"
            // "qwen2.5-coder" -> "Qwen2.5 Coder"

            // Split the ID by "-" or "_"
            val words = id.split(if (id.contains('-')) '-' else '_').toMutableList()

            // Capitalize (abbreviations) or title case (words)
            val abbreviations = setOf("gpt", "tts")

            for (i in words.indices) {
                if (words[i].lowercase() in abbreviations) {
                    words[i] = words[i].uppercase()
                } else if (words[i].contains("\\d".toRegex()) && words[i].length <= 3) {
                    // Contains a digit and is 2 characters or less
                    // Probably not a word
                } else {
                    words[i] = words[i].replaceFirstChar { it.uppercase() }
                }
            }

            // Transform words so as to merge an abbreviation with a word after it
            for (i in 0 until words.size - 1) {
                if (words[i].lowercase() in abbreviations) {
                    words[i + 1] = words[i] + words[i + 1]
                    words[i] = ""
                }
            }

            // Join the words back together
            val joined = words.filter { it.isNotBlank() }.joinToString(" ")

            return joined
        }
    }
    val displayName = name ?: getNameFromId(id)
}
