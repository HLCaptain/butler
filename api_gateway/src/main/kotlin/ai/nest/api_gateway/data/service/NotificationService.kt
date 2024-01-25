package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.notification.NotificationDto
import ai.nest.api_gateway.data.model.notification.NotificationHistoryDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.util.Attributes
import java.util.Locale
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@Single
class NotificationService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val localizationService: LocalizationService
) {
    suspend fun getUserToken(
        id: String,
        locale: Locale
    ) = client.tryToExecute<List<String>>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("tokens/user/$id")
    }

    suspend fun getAllUsersTokens(
        ids: List<String>,
        locale: Locale
    ) = client.tryToExecute<List<String>>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("tokens/users") {
            setBody(ProtoBuf.encodeToByteArray(ListSerializer(String.serializer()), ids))
        }
    }

    suspend fun deleteNotificationCollection() = client.tryToExecute<Boolean>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        method = { delete("notifications/deleteCollection") }
    )

    suspend fun saveToken(
        userId: String,
        token: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("tokens/save-token/$userId") {
            parameter("token", token)
        }
    }

    suspend fun deleteDeviceToken(
        userId: String,
        token: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        delete("device/token/$userId") {
            parameter("deviceToken", token)
        }
    }

    suspend fun clearDevicesTokens(
        userId: String,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        delete("device/allTokens/$userId")
    }

    suspend fun sendNotificationToUser(
        notificationDto: NotificationDto,
        locale: Locale
    ) = client.tryToExecute<Boolean>(
        api = APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        post("notifications/send/user") {
            setBody(notificationDto)
        }
    }

    suspend fun getNotificationHistoryForUser(
        userId: String,
        page: String,
        limit: String,
        locale: Locale
    ) = client.tryToExecute<PaginationResponse<NotificationHistoryDto>>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("notifications/history/$userId") {
            parameter("page", page)
            parameter("limit", limit)
        }
    }

    suspend fun getNotificationHistoryForUserInLast24Hours(
        userId: String,
        locale: Locale
    ) = client.tryToExecute<List<NotificationHistoryDto>>(
        APIs.NOTIFICATION_API,
        attributes = attributes,
        setErrorMessage = { localizationService.getLocalizedMessages(it, locale) }
    ) {
        get("notifications/history-24hours/$userId")
    }
}