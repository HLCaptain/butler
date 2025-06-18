package illyan.butler.core.network.ktor.http.di

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.Assistant
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantRequest
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.Transcription
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.audio.Translation
import com.aallam.openai.api.audio.TranslationRequest
import com.aallam.openai.api.batch.BatchId
import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatChunk
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatDelta
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.core.OrganizationId
import com.aallam.openai.api.core.PaginatedList
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.SortOrder
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.core.Usage
import com.aallam.openai.api.edits.Edit
import com.aallam.openai.api.edits.EditsRequest
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.embedding.EmbeddingResponse
import com.aallam.openai.api.file.File
import com.aallam.openai.api.file.FileId
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.finetune.FineTune
import com.aallam.openai.api.finetune.FineTuneEvent
import com.aallam.openai.api.finetune.FineTuneId
import com.aallam.openai.api.finetune.FineTuneRequest
import com.aallam.openai.api.finetuning.FineTuningId
import com.aallam.openai.api.finetuning.FineTuningJob
import com.aallam.openai.api.finetuning.FineTuningJobEvent
import com.aallam.openai.api.finetuning.FineTuningRequest
import com.aallam.openai.api.finetuning.Hyperparameters
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageEdit
import com.aallam.openai.api.image.ImageJSON
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.image.ImageVariation
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageId
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.moderation.Categories
import com.aallam.openai.api.moderation.CategoryScores
import com.aallam.openai.api.moderation.ModerationModel
import com.aallam.openai.api.moderation.ModerationRequest
import com.aallam.openai.api.moderation.ModerationResult
import com.aallam.openai.api.moderation.TextModeration
import com.aallam.openai.api.run.AssistantStreamEvent
import com.aallam.openai.api.run.AssistantStreamEventType
import com.aallam.openai.api.run.MessageCreation
import com.aallam.openai.api.run.MessageCreationStep
import com.aallam.openai.api.run.MessageCreationStepDetails
import com.aallam.openai.api.run.Run
import com.aallam.openai.api.run.RunId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.run.RunStep
import com.aallam.openai.api.run.RunStepId
import com.aallam.openai.api.run.ThreadRunRequest
import com.aallam.openai.api.run.ToolOutput
import com.aallam.openai.api.thread.Thread
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.thread.ThreadRequest
import com.aallam.openai.api.vectorstore.ExpirationPolicy
import com.aallam.openai.api.vectorstore.FileBatchRequest
import com.aallam.openai.api.vectorstore.FileCounts
import com.aallam.openai.api.vectorstore.FilesBatch
import com.aallam.openai.api.vectorstore.VectorStore
import com.aallam.openai.api.vectorstore.VectorStoreFile
import com.aallam.openai.api.vectorstore.VectorStoreFileRequest
import com.aallam.openai.api.vectorstore.VectorStoreId
import com.aallam.openai.api.vectorstore.VectorStoreRequest
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import illyan.butler.config.BuildConfig
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.network.ktor.http.setupCioClient
import illyan.butler.core.network.ktor.http.setupClient
import illyan.butler.data.error.ErrorRepository
import illyan.butler.shared.model.auth.ApiKeyCredential
import illyan.butler.shared.model.chat.Source
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.network.tls.CIOCipherSuites
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.io.asSource
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi

@Single
class KtorHttpClientFactory(
    private val credentialDataSource: CredentialLocalDataSource,
    private val errorRepository: ErrorRepository
) : (Source.Server) -> HttpClient {

    private val hashMap = hashMapOf<Source.Server, HttpClient>()

    @OptIn(ExperimentalUuidApi::class)
    @ExperimentalSerializationApi
    override fun invoke(source: Source.Server): HttpClient {
        return hashMap.getOrPut(source) {
            HttpClient(CIO) {
                setupCioClient()
                setupClient(
                    credentialDataSource = credentialDataSource,
                    errorRepository = errorRepository,
                    endpoint = source.endpoint,
                    userId = source.userId
                )
            }
        }
    }
}

@Single
class KtorUnauthorizedHttpClientFactory(
    private val credentialDataSource: CredentialLocalDataSource,
    private val errorRepository: ErrorRepository
) : (String) -> HttpClient {

    private val hashMap = hashMapOf<String, HttpClient>()

    @OptIn(ExperimentalUuidApi::class)
    @ExperimentalSerializationApi
    override fun invoke(endpoint: String): HttpClient {
        return hashMap.getOrPut(endpoint) {
            HttpClient(CIO) {
                setupCioClient()
                setupClient(
                    credentialDataSource = credentialDataSource,
                    errorRepository = errorRepository,
                    endpoint = endpoint,
                    userId = null
                )
            }
        }
    }
}

@Single
fun provideOpenAIClient(
    credential: ApiKeyCredential
) = if (BuildConfig.USE_MEMORY_DB) {
    // Dummy OpenAI client for testing
    object : OpenAI {
        @BetaOpenAI
        override suspend fun assistant(
            request: AssistantRequest,
            requestOptions: RequestOptions?
        ): Assistant {
            return Assistant(
                id = AssistantId(id = "dummy_assistant_id"),
                name = "Simulated Assistant",
                description = "This is a dummy assistant for testing.",
                createdAt = Clock.System.now().epochSeconds - 2.days.inWholeSeconds,
                model = ModelId(id = "dummy_model_id"),
                tools = emptyList(),
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun assistant(
            id: AssistantId,
            requestOptions: RequestOptions?
        ): Assistant? {
            return assistant(AssistantRequest(), requestOptions)
        }

        @BetaOpenAI
        override suspend fun assistant(
            id: AssistantId,
            request: AssistantRequest,
            requestOptions: RequestOptions?
        ): Assistant {
            return assistant(request, requestOptions)
        }

        @BetaOpenAI
        override suspend fun assistants(
            limit: Int?,
            order: SortOrder?,
            after: AssistantId?,
            before: AssistantId?,
            requestOptions: RequestOptions?
        ): List<Assistant> {
            return listOf(assistant(AssistantRequest(), requestOptions))
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun cancel(fineTuneId: FineTuneId): FineTune? {
            TODO("Not yet implemented")
        }

        override suspend fun cancel(
            id: FineTuningId,
            requestOptions: RequestOptions?
        ): FineTuningJob? {
            return null
        }

        @BetaOpenAI
        override suspend fun cancel(
            threadId: ThreadId,
            runId: RunId,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = threadId,
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun cancel(
            vectorStoreId: VectorStoreId,
            batchId: BatchId,
            requestOptions: RequestOptions?
        ): FilesBatch? {
            return null
        }

        override suspend fun chatCompletion(
            request: ChatCompletionRequest,
            requestOptions: RequestOptions?
        ): ChatCompletion {
            return ChatCompletion(
                id = "dummy_chat_completion_id",
                created = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                model = ModelId(id = "dummy_model_id"),
                choices = listOf(
                    ChatChoice(
                        index = 0,
                        message = ChatMessage.Assistant(
                            content = "Dummy Assistant Message",
                        )
                    )
                )
            )
        }

        override fun chatCompletions(
            request: ChatCompletionRequest,
            requestOptions: RequestOptions?
        ): Flow<ChatCompletionChunk> {
            return flowOf(ChatCompletionChunk(
                id = "dummy_chat_completion_id",
                created = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                model = ModelId(id = "dummy_model_id"),
                choices = listOf(
                    ChatChunk(
                        index = 0,
                        delta = ChatDelta(
                            role = Role.Assistant,
                            content = "Chunked Dummy Assistant Message"
                        )
                    )
                )
            ))
        }

        override fun close() {
            // No-op
        }

        @Deprecated("completions is deprecated, use chat completion instead")
        override suspend fun completion(request: CompletionRequest): TextCompletion {
            TODO("Not yet implemented")
        }

        @Deprecated("completions is deprecated, use chat completion instead")
        override fun completions(request: CompletionRequest): Flow<TextCompletion> {
            TODO("Not yet implemented")
        }

        @BetaOpenAI
        override suspend fun createRun(
            threadId: ThreadId,
            request: RunRequest,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = threadId,
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
            )
        }

        @BetaOpenAI
        override suspend fun createThreadRun(
            request: ThreadRunRequest,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = ThreadId(id = "dummy_thread_id"),
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
            )
        }

        @BetaOpenAI
        override suspend fun createVectorStore(
            request: VectorStoreRequest?,
            requestOptions: RequestOptions?
        ): VectorStore {
            return VectorStore(
                id = VectorStoreId(id = "dummy_vector_store_id"),
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                metadata = emptyMap(),
                usageBytes = 0,
                fileCounts = FileCounts(
                    inProgress = 0,
                    completed = 0,
                    failed = 0,
                    total = 0,
                    cancelled = 0
                ),
                name = "Dummy Vector Store",
                status = Status.Completed,
                expiresAfter = ExpirationPolicy(
                    anchor = "last_active_at",
                    days = 0
                ),
                expiresAt = 0,
                lastActiveAt = 0,
            )
        }

        @BetaOpenAI
        override suspend fun createVectorStoreFile(
            id: VectorStoreId,
            request: VectorStoreFileRequest,
            requestOptions: RequestOptions?
        ): VectorStoreFile {
            return VectorStoreFile(
                id = FileId(id = "dummy_file_id"),
                vectorStoreId = id,
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                status = Status.Completed,
                usageBytes = 0,
            )
        }

        @BetaOpenAI
        override suspend fun createVectorStoreFilesBatch(
            id: VectorStoreId,
            request: FileBatchRequest,
            requestOptions: RequestOptions?
        ): FilesBatch {
            return FilesBatch(
                id = BatchId(id = "dummy_batch_id"),
                vectorStoreId = id,
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                status = Status.Completed,
                fileCounts = FileCounts(
                    inProgress = 0,
                    completed = 0,
                    failed = 0,
                    total = 0,
                    cancelled = 0
                )
            )
        }

        override suspend fun delete(fileId: FileId, requestOptions: RequestOptions?): Boolean {
            return true
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun delete(fineTuneModel: ModelId): Boolean {
            TODO("Not yet implemented")
        }

        @BetaOpenAI
        override suspend fun delete(id: AssistantId, requestOptions: RequestOptions?): Boolean {
            return true
        }

        @BetaOpenAI
        override suspend fun delete(id: ThreadId, requestOptions: RequestOptions?): Boolean {
            return true
        }

        @BetaOpenAI
        override suspend fun delete(id: VectorStoreId, requestOptions: RequestOptions?): Boolean {
            return true
        }

        @BetaOpenAI
        override suspend fun delete(
            id: VectorStoreId,
            fileId: FileId,
            requestOptions: RequestOptions?
        ): Boolean {
            return true
        }

        override suspend fun download(fileId: FileId, requestOptions: RequestOptions?): ByteArray {
            return byteArrayOf()
        }

        @Deprecated("Edits is deprecated. Chat completions is the recommend replacement.")
        override suspend fun edit(request: EditsRequest): Edit {
            TODO("Not yet implemented")
        }

        override suspend fun embeddings(
            request: EmbeddingRequest,
            requestOptions: RequestOptions?
        ): EmbeddingResponse {
            return EmbeddingResponse(
                embeddings = emptyList(),
                usage = Usage(
                    promptTokens = 0,
                    completionTokens = 0,
                    totalTokens = 0
                )
            )
        }

        override suspend fun file(request: FileUpload, requestOptions: RequestOptions?): File {
            return File(
                id = FileId(id = "dummy_file_id"),
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                status = Status.Completed,
                bytes = 0,
                filename = "dummy_file.txt",
                purpose = Purpose("dummy_purpose"),
            )
        }

        override suspend fun file(fileId: FileId, requestOptions: RequestOptions?): File? {
            return file(FileUpload(
                file = FileSource(
                    name = "dummy_file.txt",
                    source = byteArrayOf().inputStream().asSource()
                ),
                purpose = Purpose("dummy_purpose")
            ), requestOptions)
        }

        override suspend fun files(requestOptions: RequestOptions?): List<File> {
            return listOf(file(FileUpload(
                file = FileSource(
                    name = "dummy_file.txt",
                    source = byteArrayOf().inputStream().asSource()
                ),
                purpose = Purpose("dummy_purpose")
            ), requestOptions))
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun fineTune(request: FineTuneRequest): FineTune {
            TODO("Not yet implemented")
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun fineTune(fineTuneId: FineTuneId): FineTune? {
            TODO("Not yet implemented")
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun fineTuneEvents(fineTuneId: FineTuneId): List<FineTuneEvent> {
            TODO("Not yet implemented")
        }

        @Deprecated("Use FineTuning instead.")
        override fun fineTuneEventsFlow(fineTuneId: FineTuneId): Flow<FineTuneEvent> {
            TODO("Not yet implemented")
        }

        @Deprecated("Use FineTuning instead.")
        override suspend fun fineTunes(): List<FineTune> {
            TODO("Not yet implemented")
        }

        override suspend fun fineTuningEvents(
            id: FineTuningId,
            after: String?,
            limit: Int?,
            requestOptions: RequestOptions?
        ): PaginatedList<FineTuningJobEvent> {
            return PaginatedList(
                data = emptyList(),
            )
        }

        override suspend fun fineTuningJob(
            request: FineTuningRequest,
            requestOptions: RequestOptions?
        ): FineTuningJob {
            return FineTuningJob(
                id = FineTuningId(id = "dummy_fine_tuning_id"),
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                status = Status.Completed,
                organizationId = OrganizationId(id = "dummy_organization_id"),
                hyperparameters = Hyperparameters(Hyperparameters.NEpochs.Auto),
                trainingFile = FileId(id = "dummy_file_id"),
                resultFiles = emptyList(),
            )
        }

        override suspend fun fineTuningJob(
            id: FineTuningId,
            requestOptions: RequestOptions?
        ): FineTuningJob? {
            return null
        }

        override suspend fun fineTuningJobs(
            after: String?,
            limit: Int?,
            requestOptions: RequestOptions?
        ): List<FineTuningJob> {
            return emptyList()
        }

        @BetaOpenAI
        override suspend fun getRun(
            threadId: ThreadId,
            runId: RunId,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = threadId,
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
                metadata = emptyMap()
            )
        }

        override suspend fun imageJSON(
            creation: ImageCreation,
            requestOptions: RequestOptions?
        ): List<ImageJSON> {
            return listOf(ImageJSON(b64JSON = "dummy_b64_json"))
        }

        override suspend fun imageJSON(
            edit: ImageEdit,
            requestOptions: RequestOptions?
        ): List<ImageJSON> {
            return listOf(ImageJSON(b64JSON = "dummy_b64_json"))
        }

        override suspend fun imageJSON(
            variation: ImageVariation,
            requestOptions: RequestOptions?
        ): List<ImageJSON> {
            return listOf(ImageJSON(b64JSON = "dummy_b64_json"))
        }

        override suspend fun imageURL(
            creation: ImageCreation,
            requestOptions: RequestOptions?
        ): List<ImageURL> {
            return listOf(ImageURL(url = "https://dummy.url"))
        }

        override suspend fun imageURL(
            edit: ImageEdit,
            requestOptions: RequestOptions?
        ): List<ImageURL> {
            return listOf(ImageURL(url = "https://dummy.url"))
        }

        override suspend fun imageURL(
            variation: ImageVariation,
            requestOptions: RequestOptions?
        ): List<ImageURL> {
            return listOf(ImageURL(url = "https://dummy.url"))
        }

        @BetaOpenAI
        override suspend fun message(
            threadId: ThreadId,
            request: MessageRequest,
            requestOptions: RequestOptions?
        ): Message {
            return Message(
                id = MessageId(id = "dummy_message_id"),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                threadId = threadId,
                role = ChatRole.User,
                content = listOf(
                    MessageContent.Text(
                        text = com.aallam.openai.api.message.TextContent(
                            value = "Dummy Message",
                            annotations = emptyList(),
                        ),
                    )
                ),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun message(
            threadId: ThreadId,
            messageId: MessageId,
            requestOptions: RequestOptions?
        ): Message {
            return Message(
                id = messageId,
                assistantId = AssistantId(id = "dummy_assistant_id"),
                threadId = threadId,
                role = ChatRole.User,
                content = listOf(
                    MessageContent.Text(
                        text = com.aallam.openai.api.message.TextContent(
                            value = "Dummy Message",
                            annotations = emptyList(),
                        ),
                    )
                ),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun message(
            threadId: ThreadId,
            messageId: MessageId,
            metadata: Map<String, String>?,
            requestOptions: RequestOptions?
        ): Message {
            return Message(
                id = messageId,
                assistantId = AssistantId(id = "dummy_assistant_id"),
                threadId = threadId,
                role = ChatRole.User,
                content = listOf(
                    MessageContent.Text(
                        text = com.aallam.openai.api.message.TextContent(
                            value = "Dummy Message",
                            annotations = emptyList(),
                        ),
                    )
                ),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun messages(
            threadId: ThreadId,
            limit: Int?,
            order: SortOrder?,
            after: MessageId?,
            before: MessageId?,
            requestOptions: RequestOptions?
        ): List<Message> {
            return listOf(
                Message(
                    id = MessageId(id = "dummy_message_id"),
                    assistantId = AssistantId(id = "dummy_assistant_id"),
                    threadId = threadId,
                    role = ChatRole.User,
                    content = listOf(
                        MessageContent.Text(
                            text = com.aallam.openai.api.message.TextContent(
                                value = "Dummy Message",
                                annotations = emptyList(),
                            ),
                        )
                    ),
                    createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                    metadata = emptyMap()
                )
            )
        }

        override suspend fun model(modelId: ModelId, requestOptions: RequestOptions?): Model {
            return Model(
                id = modelId,
                created = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                ownedBy = "dummy_organization_id",
                permission = emptyList()
            )
        }

        override suspend fun models(requestOptions: RequestOptions?): List<Model> {
            return listOf(
                Model(
                    id = ModelId(id = "dummy_model_id"),
                    created = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                    ownedBy = "dummy_organization_id",
                    permission = emptyList()
                )
            )
        }

        override suspend fun moderations(
            request: ModerationRequest,
            requestOptions: RequestOptions?
        ): TextModeration {
            return TextModeration(
                id = "dummy_moderation_id",
                model = ModerationModel.Stable,
                results = listOf(
                    ModerationResult(
                        categories = Categories(
                            hate = false,
                            hateThreatening = false,
                            selfHarm = false,
                            sexual = false,
                            sexualMinors = false,
                            violence = false,
                            violenceGraphic = false,
                            harassment = false,
                            harassmentThreatening = false,
                            selfHarmIntent = false,
                            selfHarmInstructions = false
                        ),
                        categoryScores = CategoryScores(
                            hate = 0.0,
                            hateThreatening = 0.0,
                            selfHarm = 0.0,
                            sexual = 0.0,
                            sexualMinors = 0.0,
                            violence = 0.0,
                            violenceGraphic = 0.0,
                            harassment = 0.0,
                            harassmentThreatening = 0.0,
                            selfHarmIntent = 0.0,
                            selfHarmInstructions = 0.0
                        ),
                        flagged = false
                    ),

                )
            )
        }

        @BetaOpenAI
        override suspend fun runStep(
            threadId: ThreadId,
            runId: RunId,
            stepId: RunStepId,
            requestOptions: RequestOptions?
        ): RunStep {
            return MessageCreationStep(
                id = RunStepId(id = "dummy_run_step_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                threadId = threadId,
                runId = runId,
                status = Status.Completed,
                stepDetails = MessageCreationStepDetails(
                    messageCreation = MessageCreation(
                        messageId = MessageId(id = "dummy_message_id"),
                    )
                )
            )
        }

        @BetaOpenAI
        override suspend fun runSteps(
            threadId: ThreadId,
            runId: RunId,
            limit: Int?,
            order: SortOrder?,
            after: RunStepId?,
            before: RunStepId?,
            requestOptions: RequestOptions?
        ): List<RunStep> {
            return listOf(
                MessageCreationStep(
                    id = RunStepId(id = "dummy_run_step_id"),
                    createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                    assistantId = AssistantId(id = "dummy_assistant_id"),
                    threadId = threadId,
                    runId = runId,
                    status = Status.Completed,
                    stepDetails = MessageCreationStepDetails(
                        messageCreation = MessageCreation(
                            messageId = MessageId(id = "dummy_message_id"),
                        )
                    )
                )
            )
        }

        @BetaOpenAI
        override suspend fun runs(
            threadId: ThreadId,
            limit: Int?,
            order: SortOrder?,
            after: RunId?,
            before: RunId?,
            requestOptions: RequestOptions?
        ): List<Run> {
            return listOf(
                Run(
                    id = RunId(id = "dummy_run_id"),
                    threadId = threadId,
                    model = ModelId(id = "dummy_model_id"),
                    createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                    assistantId = AssistantId(id = "dummy_assistant_id"),
                    status = Status.Completed,
                    metadata = emptyMap()
                )
            )
        }

        override suspend fun speech(
            request: SpeechRequest,
            requestOptions: RequestOptions?
        ): ByteArray {
            return byteArrayOf()
        }

        @BetaOpenAI
        override suspend fun submitToolOutput(
            threadId: ThreadId,
            runId: RunId,
            output: List<ToolOutput>,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = threadId,
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun thread(
            request: ThreadRequest?,
            requestOptions: RequestOptions?
        ): Thread {
            return Thread(
                id = ThreadId(id = "dummy_thread_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun thread(id: ThreadId, requestOptions: RequestOptions?): Thread? {
            return thread(ThreadRequest(), requestOptions)
        }

        @BetaOpenAI
        override suspend fun thread(
            id: ThreadId,
            metadata: Map<String, String>,
            requestOptions: RequestOptions?
        ): Thread {
            return thread(ThreadRequest(), requestOptions)
        }

        override suspend fun transcription(
            request: TranscriptionRequest,
            requestOptions: RequestOptions?
        ): Transcription {
            return Transcription(text = "Dummy Transcription")
        }

        override suspend fun translation(
            request: TranslationRequest,
            requestOptions: RequestOptions?
        ): Translation {
            return Translation(text = "Dummy Translation")
        }

        @BetaOpenAI
        override suspend fun updateRun(
            threadId: ThreadId,
            runId: RunId,
            metadata: Map<String, String>?,
            requestOptions: RequestOptions?
        ): Run {
            return Run(
                id = RunId(id = "dummy_run_id"),
                threadId = threadId,
                model = ModelId(id = "dummy_model_id"),
                createdAt = (Clock.System.now().epochSeconds - 1.days.inWholeSeconds).toInt(),
                assistantId = AssistantId(id = "dummy_assistant_id"),
                status = Status.Completed,
                metadata = emptyMap()
            )
        }

        @BetaOpenAI
        override suspend fun updateVectorStore(
            id: VectorStoreId,
            request: VectorStoreRequest,
            requestOptions: RequestOptions?
        ): VectorStore {
            return VectorStore(
                id = id,
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                metadata = emptyMap(),
                usageBytes = 0,
                fileCounts = FileCounts(
                    inProgress = 0,
                    completed = 0,
                    failed = 0,
                    total = 0,
                    cancelled = 0
                ),
                name = "Dummy Vector Store",
                status = Status.Completed,
                expiresAfter = ExpirationPolicy(
                    anchor = "last_active_at",
                    days = 0
                ),
                expiresAt = 0,
                lastActiveAt = 0,
            )
        }

        @BetaOpenAI
        override suspend fun vectorStore(
            id: VectorStoreId,
            requestOptions: RequestOptions?
        ): VectorStore? {
            return VectorStore(
                id = id,
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                metadata = emptyMap(),
                usageBytes = 0,
                fileCounts = FileCounts(
                    inProgress = 0,
                    completed = 0,
                    failed = 0,
                    total = 0,
                    cancelled = 0
                ),
                name = "Dummy Vector Store",
                status = Status.Completed,
                expiresAfter = ExpirationPolicy(
                    anchor = "last_active_at",
                    days = 0
                ),
                expiresAt = 0,
                lastActiveAt = 0,
            )
        }

        @BetaOpenAI
        override suspend fun vectorStoreFileBatch(
            vectorStoreId: VectorStoreId,
            batchId: BatchId,
            requestOptions: RequestOptions?
        ): FilesBatch? {
            return FilesBatch(
                id = batchId,
                createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                vectorStoreId = vectorStoreId,
                status = Status.Completed,
                fileCounts = FileCounts(
                    inProgress = 0,
                    completed = 0,
                    failed = 0,
                    total = 0,
                    cancelled = 0
                )
            )
        }

        @BetaOpenAI
        override suspend fun vectorStoreFiles(
            id: VectorStoreId,
            limit: Int?,
            order: SortOrder?,
            after: VectorStoreId?,
            before: VectorStoreId?,
            filter: Status?,
            requestOptions: RequestOptions?
        ): List<VectorStoreFile> {
            return listOf(
                VectorStoreFile(
                    id = FileId(id = "dummy_file_id"),
                    vectorStoreId = id,
                    createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                    status = Status.Completed,
                    usageBytes = 0,
                )
            )
        }

        @BetaOpenAI
        override suspend fun vectorStoreFilesBatches(
            vectorStoreId: VectorStoreId,
            batchId: BatchId,
            limit: Int?,
            order: SortOrder?,
            after: VectorStoreId?,
            before: VectorStoreId?,
            filter: Status?,
            requestOptions: RequestOptions?
        ): List<VectorStoreFile> {
            return listOf(
                VectorStoreFile(
                    id = FileId(id = "dummy_file_id"),
                    vectorStoreId = vectorStoreId,
                    createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                    status = Status.Completed,
                    usageBytes = 0,
                )
            )
        }

        @BetaOpenAI
        override suspend fun vectorStores(
            limit: Int?,
            order: SortOrder?,
            after: VectorStoreId?,
            before: VectorStoreId?,
            requestOptions: RequestOptions?
        ): List<VectorStore> {
            return listOf(
                VectorStore(
                    id = VectorStoreId(id = "dummy_vector_store_id"),
                    createdAt = Clock.System.now().epochSeconds - 1.days.inWholeSeconds,
                    metadata = emptyMap(),
                    usageBytes = 0,
                    fileCounts = FileCounts(
                        inProgress = 0,
                        completed = 0,
                        failed = 0,
                        total = 0,
                        cancelled = 0
                    ),
                    name = "Dummy Vector Store",
                    status = Status.Completed,
                    expiresAfter = ExpirationPolicy(
                        anchor = "last_active_at",
                        days = 0
                    ),
                    expiresAt = 0,
                    lastActiveAt = 0,
                )
            )
        }

        @BetaOpenAI
        override suspend fun createStreamingRun(
            threadId: ThreadId,
            request: RunRequest,
            requestOptions: RequestOptions?
        ): Flow<AssistantStreamEvent> {
            return flowOf(
                AssistantStreamEvent(
                    rawType = "dummy_type",
                    type = AssistantStreamEventType.THREAD_CREATED,
                    data = "dummy_data",
                )
            )
        }

        @BetaOpenAI
        override suspend fun createStreamingThreadRun(
            request: ThreadRunRequest,
            requestOptions: RequestOptions?
        ): Flow<AssistantStreamEvent> {
            return flowOf(
                AssistantStreamEvent(
                    rawType = "dummy_type",
                    type = AssistantStreamEventType.THREAD_CREATED,
                    data = "dummy_data",
                )
            )
        }

        @BetaOpenAI
        override suspend fun submitStreamingToolOutput(
            threadId: ThreadId,
            runId: RunId,
            output: List<ToolOutput>,
            requestOptions: RequestOptions?
        ): Flow<AssistantStreamEvent> {
            return flowOf(
                AssistantStreamEvent(
                    rawType = "dummy_type",
                    type = AssistantStreamEventType.THREAD_CREATED,
                    data = "dummy_data",
                )
            )
        }
    }
} else {
    OpenAI(
        config = OpenAIConfig(
            token = credential.apiKey,
            host = OpenAIHost(baseUrl = credential.providerUrl + if (credential.providerUrl.endsWith("/")) "" else "/"),
            engine = CIO.create {
                https {
                    serverName = null // Dynamically infer from URL
                    cipherSuites = CIOCipherSuites.SupportedSuites
                }
            }
        )
    )
}
