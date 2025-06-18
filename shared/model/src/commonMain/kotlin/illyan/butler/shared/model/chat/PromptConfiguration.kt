@file:OptIn(ExperimentalTime::class)

package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class PromptConfiguration(
    val name: String,
    val description: String? = null,
    val formatInstructions: String? = null,
    val variables: List<PromptVariable> = emptyList(),
) {
    companion object {
        val Default = PromptConfiguration(
            name = "Default",
            description = "Default prompt configuration for AI models.",
            formatInstructions = "Please provide a response in a concise and informative manner.",
            variables = listOf(
                PromptVariable.Regular("User Name", "User"),
                PromptVariable.Regular("AI Model", "Generic AI"),
                PromptVariable.Traits(listOf("helpful", "informative", "concise")),
            ),
        )
    }
}

@Serializable
sealed class PromptVariable {
    abstract val title: String
    abstract val defaultValue: String

    @Serializable
    data class Regular(
        override val title: String,
        override val defaultValue: String,
    ) : PromptVariable() {
        override fun toString(): String {
            return super.toString()
        }
    }

    @Serializable
    data class Traits(
        val traits: List<String> = emptyList(),
    ) : PromptVariable() {
        override val title: String
            get() = "Traits of AI model"
        override val defaultValue: String
            get() = traits.joinToString(", ")
        override fun toString(): String {
            return super.toString()
        }
    }

    override fun toString(): String {
        return "$title: $defaultValue"
    }
}
