package illyan.butler.shared.llm

@OptIn(markerClass = [kotlin.uuid.ExperimentalUuidApi::class])
actual fun provideLlmService(
    coroutineScopeIO: CoroutineScope,
    getResource: suspend (Uuid, Uuid) -> ResourceDto,
    createResource: suspend (Uuid, SenderType.Ai, ResourceDto) -> ResourceDto,
    upsertMessage: suspend (MessageDto) -> MessageDto,
    getOpenAIClient: suspend (String) -> OpenAI,
    upsertChat: suspend (ChatDto) -> ChatDto,
    errorInMessageResponse: suspend (MessageDto?) -> Unit,
    removeMessage: suspend (MessageDto) -> Unit
): LlmService? {
    return null // iOS does not support LLM services directly
}