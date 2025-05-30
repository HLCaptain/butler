package illyan.butler.server.di

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import illyan.butler.server.AppConfig
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes

@Named("OpenAIClients")
@Single
fun provideOpenAIClients(): Map<String, OpenAI> {
    return AppConfig.Api.OPEN_AI_API_URLS_AND_KEYS.mapValues { (url, key) ->
        OpenAI(
            config = OpenAIConfig(
                token = key,
                logging = LoggingConfig(LogLevel.All, Logger.Default),
//                engine = client.engine,
                host = OpenAIHost(baseUrl = if (url.endsWith('/')) url else "$url/"), // Ensure URL ends with '/'
                timeout = Timeout(
                    request = 10.minutes,
                    connect = 10.minutes,
                    socket = 10.minutes
                )
            )
        )
    }
}
