package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.notification.NotificationDto
import ai.nest.api_gateway.data.model.notification.NotificationHistoryDto
import ai.nest.api_gateway.data.model.response.PaginationResponse
import ai.nest.api_gateway.data.utils.bodyOrThrow
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import org.koin.core.annotation.Single

@Single
class NotificationService(private val client: HttpClient) {
    suspend fun getUserToken(id: String) = client
        .get("${AppConfig.Api.NOTIFICATION_API_URL}/tokens/user/$id")
        .bodyOrThrow<List<String>>()

    suspend fun getAllUsersTokens(ids: List<String>) = client.post(
        "${AppConfig.Api.NOTIFICATION_API_URL}/tokens/users"
    ) { setBody(ids) }.bodyOrThrow<List<String>>()

    suspend fun deleteNotificationCollection() = client.delete(
        "${AppConfig.Api.NOTIFICATION_API_URL}/notifications/deleteCollection"
    ).status.isSuccess()

    suspend fun saveToken(userId: String, token: String) = client.post(
        "${AppConfig.Api.NOTIFICATION_API_URL}/tokens/save-token/$userId"
    ) { parameter("token", token) }.status.isSuccess()

    suspend fun deleteDeviceToken(userId: String, token: String) = client.delete(
        "${AppConfig.Api.NOTIFICATION_API_URL}/device/token/$userId"
    ) { parameter("deviceToken", token) }.status.isSuccess()


    suspend fun clearDevicesTokens(userId: String) = client
        .delete("${AppConfig.Api.NOTIFICATION_API_URL}/device/allTokens/$userId")
        .status.isSuccess()

    suspend fun sendNotificationToUser(notificationDto: NotificationDto) = client.post(
        "${AppConfig.Api.NOTIFICATION_API_URL}/notifications/send/user"
    ) { setBody(notificationDto) }.status.isSuccess()

    suspend fun getNotificationHistoryForUser(userId: String, page: String, limit: String) =
        client.get("${AppConfig.Api.NOTIFICATION_API_URL}/notifications/history/$userId") {
            parameter("page", page)
            parameter("limit", limit)
        }.bodyOrThrow<PaginationResponse<NotificationHistoryDto>>()

    suspend fun getNotificationHistoryForUserInLast24Hours(userId: String) = client
        .get("${AppConfig.Api.NOTIFICATION_API_URL}/notifications/history-24hours/$userId")
        .bodyOrThrow<List<NotificationHistoryDto>>()
}