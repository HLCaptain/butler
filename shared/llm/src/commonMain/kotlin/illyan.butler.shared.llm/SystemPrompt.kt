package illyan.butler.shared.llm

import illyan.butler.shared.model.chat.PromptConfiguration
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Utility class for generating system prompts for LLM interactions
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class SystemPromptBuilder {
    private var instructions = ""
    private var modelName = "Butler AI Assistant"
    private var capabilities = mutableListOf<String>()
    private var contextItems = mutableListOf<String>()
    private var formatInstructions = ""
    private var userInfo = mutableMapOf<String, String>()
    private var promptConfiguration: PromptConfiguration? = null

    fun withConfiguration(promptConfiguration: PromptConfiguration): SystemPromptBuilder {
        this.promptConfiguration = promptConfiguration
        return this
    }

    /**
     * Set the name for the AI model to use in responses
     */
    fun withModelName(name: String): SystemPromptBuilder {
        modelName = name
        return this
    }

    /**
     * Add primary instructions for the model
     */
    fun withInstructions(instructionsText: String): SystemPromptBuilder {
        instructions = instructionsText
        return this
    }

    /**
     * Add a capability that the model can use
     */
    fun withCapability(capability: String): SystemPromptBuilder {
        capabilities.add(capability)
        return this
    }

    /**
     * Add multiple capabilities at once
     */
    fun withCapabilities(capabilityList: List<String>): SystemPromptBuilder {
        capabilities.addAll(capabilityList)
        return this
    }

    /**
     * Add context information for the current conversation
     */
    fun withContext(contextItem: String): SystemPromptBuilder {
        contextItems.add(contextItem)
        return this
    }

    /**
     * Add format instructions for specific response requirements
     */
    fun withFormatInstructions(formatText: String): SystemPromptBuilder {
        formatInstructions = formatText
        return this
    }

    /**
     * Add user-specific information
     */
    fun withUserInfo(key: String, value: String): SystemPromptBuilder {
        userInfo[key] = value
        return this
    }

    /**
     * Build the complete system prompt
     */
    fun build(sessionId: Uuid? = null): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDate = "${now.date}"
        val currentTime = "${now.hour}:${now.minute}"
        val configDescription = promptConfiguration?.description
        val variables = promptConfiguration?.variables

        var introSection = """
            |You are $modelName, an advanced AI assistant.
            |Current date: $currentDate
            |Current time: $currentTime
            |${if (sessionId != null) "Session ID: $sessionId" else ""}
        """.trimMargin()

        if (configDescription != null) {
            introSection += "\n|Configuration: $configDescription"
        }
        if (variables != null) {
            introSection += "\n|Variables:"
            variables.forEach { variable ->
                introSection += "\n| $variable"
            }
        }

        val userInfoSection = if (userInfo.isNotEmpty()) {
            """
            |USER INFORMATION:
            |${userInfo.entries.joinToString("\n|") { (key, value) -> "$key: $value" }}
            """.trimMargin()
        } else ""

        val capabilitiesSection = if (capabilities.isNotEmpty()) {
            """
            |CAPABILITIES:
            |${capabilities.joinToString("\n|") { "- $it" }}
            """.trimMargin()
        } else ""

        val contextSection = if (contextItems.isNotEmpty()) {
            """
            |CONTEXT:
            |${contextItems.joinToString("\n|") { it }}
            """.trimMargin()
        } else ""

        val instructionsSection = if (instructions.isNotEmpty()) {
            """
            |INSTRUCTIONS:
            |$instructions
            """.trimMargin()
        } else ""

        val formatSection = if (formatInstructions.isNotEmpty()) {
            """
            |RESPONSE FORMAT:
            |$formatInstructions
            """.trimMargin()
        } else ""

        // Assemble the complete prompt with sections separated by blank lines
        return listOf(
            introSection,
            userInfoSection,
            capabilitiesSection,
            contextSection,
            instructionsSection,
            formatSection
        ).filter { it.isNotEmpty() }.joinToString("\n\n")
    }
}

/**
 * Generate a comprehensive system prompt for LLM interactions
 *
 * @param chatId The ID of the current chat
 * @param userName The name of the user (optional)
 * @param customInstructions Any custom instructions to include
 * @param formatRequirements Specific format requirements for the response
 * @return A formatted system prompt string
 */
@OptIn(ExperimentalUuidApi::class)
fun generateSystemPrompt(
    chatId: Uuid,
    userName: String? = null,
    customInstructions: String? = null,
    formatRequirements: String? = null
): String {
    val builder = SystemPromptBuilder()
        .withModelName("Butler AI Assistant")
        .withInstructions("""
            You are Butler AI Assistant, designed to be helpful, accurate, and ethical.
            Always provide truthful information and be transparent about your limitations.
            Keep your responses concise and relevant to the user's query.
            ${customInstructions ?: ""}
        """.trimIndent())
        .withCapabilities(listOf(
            "Answer general knowledge questions",
            "Engage in conversation on various topics",
            "Provide summaries of information",
            "Generate creative content like stories or poems",
            "Help with problem-solving and brainstorming",
            "Remember context from earlier in our conversation"
        ))
        .withContext("This is conversation ID: $chatId")

    // Add user information if available
    userName?.let { builder.withUserInfo("User Name", it) }

    // Add format instructions if provided
    formatRequirements?.let { builder.withFormatInstructions(it) }

    return builder.build(chatId)
}

/**
 * Generate a generic system prompt for general use
 *
 * @return A formatted generic system prompt
 */
@OptIn(ExperimentalUuidApi::class)
fun generateGenericSystemPrompt(): String {
    return SystemPromptBuilder()
        .withModelName("Butler AI Assistant")
        .withInstructions("""
            You are Butler AI Assistant, designed to be helpful, accurate, and ethical.
            Respond to the user's queries in a clear, concise manner.
            If you're uncertain about something, acknowledge your limitations rather than providing potentially incorrect information.
            Prioritize being helpful and providing accurate information.
        """.trimIndent())
        .withCapabilities(listOf(
            "Answer general knowledge questions",
            "Engage in conversation on various topics",
            "Provide explanations on a wide range of subjects",
            "Offer helpful suggestions based on user queries"
        ))
        .build()
}

/**
 * Generate a system prompt specifically for creative writing tasks
 *
 * @param genre Optional genre specification for the creative context
 * @return A formatted creative writing system prompt
 */
@OptIn(ExperimentalUuidApi::class)
fun generateCreativeWritingPrompt(genre: String? = null): String {
    val genreContext = genre?.let { "You are focusing on the $it genre in this conversation." } ?: ""

    return SystemPromptBuilder()
        .withModelName("Butler Creative Assistant")
        .withInstructions("""
            You are Butler Creative Assistant, specialized in creative writing and storytelling.
            Help the user with creative content, storytelling, and writing tasks.
            $genreContext
            Be imaginative while maintaining coherence and quality in your creative outputs.
        """.trimIndent())
        .withCapabilities(listOf(
            "Generate creative stories, poems, and narratives",
            "Provide writing suggestions and improvements",
            "Help develop characters, plots, and settings",
            "Assist with creative blocks and brainstorming",
            "Offer literary analysis and insights"
        ))
        .build()
}

/**
 * Generate a system prompt for technical assistance
 *
 * @param domain The specific technical domain (e.g., "programming", "data science")
 * @return A formatted technical assistance system prompt
 */
@OptIn(ExperimentalUuidApi::class)
fun generateTechnicalAssistancePrompt(domain: String): String {
    return SystemPromptBuilder()
        .withModelName("Butler Technical Assistant")
        .withInstructions("""
            You are Butler Technical Assistant, specialized in providing technical help in $domain.
            Provide accurate, clear, and helpful technical information.
            Include relevant code examples or technical details where appropriate.
            If asked for code, ensure it follows best practices and is well-commented.
        """.trimIndent())
        .withCapabilities(listOf(
            "Explain technical concepts clearly",
            "Provide code examples and solutions",
            "Debug issues and offer troubleshooting steps",
            "Recommend best practices and resources",
            "Guide through technical processes step by step"
        ))
        .withFormatInstructions("""
            When providing code examples:
            1. Use appropriate syntax highlighting
            2. Include comments explaining key parts
            3. Explain how the code works after the example
        """.trimIndent())
        .build()
}
