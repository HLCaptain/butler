package illyan.butler.services.ai.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import illyan.butler.services.ai.AppConfig
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes

// TODO: Add several more OpenAI API endpoints if needed
@Single
fun provideOpenAiClient(client: HttpClient): OpenAI {
    return OpenAI(
        config = OpenAIConfig(
            token = AppConfig.Api.OPEN_AI_API_KEY,
            logging = LoggingConfig(LogLevel.All, Logger.Default),
            engine = client.engine,
            host = OpenAIHost(baseUrl = AppConfig.Api.LOCAL_AI_OPEN_AI_API_URL),
            timeout = Timeout(
                request = 10.minutes,
                connect = 10.minutes,
                socket = 10.minutes
            )
        )
    )
}

@Single
fun provideOpenAIClients(client: HttpClient): Map<String, OpenAI> {
    return AppConfig.Api.OPEN_AI_API_URLS.associateWith { url ->
        OpenAI(
            config = OpenAIConfig(
                token = AppConfig.Api.OPEN_AI_API_KEY,
                logging = LoggingConfig(LogLevel.All, Logger.Default),
                engine = client.engine,
                host = OpenAIHost(baseUrl = url),
                timeout = Timeout(
                    request = 10.minutes,
                    connect = 10.minutes,
                    socket = 10.minutes
                )
            )
        )
    }
}