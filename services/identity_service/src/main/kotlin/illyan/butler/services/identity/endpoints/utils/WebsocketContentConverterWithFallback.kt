package illyan.butler.services.identity.endpoints.utils

import io.github.aakira.napier.Napier
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.WebsocketConverterNotFoundException
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import io.ktor.websocket.Frame

class WebsocketContentConverterWithFallback(
    private val contentConverters: List<WebsocketContentConverter>
) : WebsocketContentConverter {
    private val associatedFrames = mutableMapOf<String, WebsocketContentConverter>()
    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: Frame): Any? {
//        val key = "something"
//        if (associatedFrames.contains(key)) {
//            return associatedFrames[key]!!.deserialize(charset, typeInfo, content)
//        }
        val deserializedWithConverter = contentConverters.firstNotNullOfOrNull {
            try {
                val data = it.deserialize(charset, typeInfo, content)
                Napier.v { "Deserialized data: $data with converter $it" }
                data to it
            } catch (e: ContentConvertException) {
                // This converter is not applicable, find another one.
                // TODO: Maybe not throw every other exception?
                Napier.e(e) { "Error in content conversion" }
                null
            } catch (e: WebsocketDeserializeException) {
                Napier.e(e) { "Error in deserializing data" }
                null
            }
        }
        if (deserializedWithConverter != null) {
//            associatedFrames[key] = deserializedWithConverter.second
            return deserializedWithConverter.first
        } else {
            throw WebsocketConverterNotFoundException("Could not fallback to proper content converter.")
        }
    }

    override fun isApplicable(frame: Frame): Boolean {
        return contentConverters.any { it.isApplicable(frame) }
    }

    override suspend fun serializeNullable(charset: Charset, typeInfo: TypeInfo, value: Any?): Frame {
        val serializedWithConverter = contentConverters.firstNotNullOfOrNull {
            try {
                val frame = it.serializeNullable(charset, typeInfo, value)
                Napier.v { "Serialized data: $value with converter $it" }
                frame to it
            } catch (e: ContentConvertException) {
                // This converter is not applicable, find another one.
                Napier.e(e) { "Error in content conversion" }
                null
            }
        }
        if (serializedWithConverter != null) {
            return serializedWithConverter.first
        } else {
            throw WebsocketConverterNotFoundException("Could not fallback to proper content converter.")
        }
    }
}