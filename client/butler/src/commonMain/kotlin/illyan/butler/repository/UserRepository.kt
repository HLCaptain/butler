package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.ktorfit.api.AuthApi
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.identity.UserDetailsDto
import illyan.butler.di.NamedCoroutineScopeIO
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class)
@Single
class UserRepository(
    private val authApi: AuthApi,
    private val settings: FlowSettings,
    @NamedCoroutineScopeIO private val coroutineScope: CoroutineScope
) {
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_AUTH_PROVIDER = "auth_provider"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACCESS_TOKEN_EXPIRATION = "access_token_expiration"
        const val KEY_REFRESH_TOKEN_EXPIRATION = "refresh_token_expiration"
    }

    @OptIn(ExperimentalSettingsApi::class)
    val isUserSignedIn = settings.getStringOrNullFlow(KEY_USER_ID).map { it != null }

    @OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
    val userData = settings.getStringOrNullFlow(KEY_USER_ID).map { encodedUser ->
        encodedUser?.let { ProtoBuf.decodeFromByteArray(UserDetailsDto.serializer(), encodedUser.toByteArray()) }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    val signedInUserUUID = userData.map { it?.id }
    val signedInUserEmail = userData.map { it?.email }
    val signedInUserPhoneNumber = userData.map { it?.phone }
    val signedInUserPhotoURL = userData.map { it?.photoUrl }
    val signedInUserName = userData.map { it?.username }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun loginWithEmailAndPassword(email: String, password: String) {
        val response = authApi.login(UserLoginDto(email, password))
        settings.putString(KEY_AUTH_PROVIDER, "butler_api")
        settings.putString(KEY_ACCESS_TOKEN, response.accessToken)
        settings.putString(KEY_REFRESH_TOKEN, response.refreshToken)
        settings.putLong(KEY_ACCESS_TOKEN_EXPIRATION, response.accessTokenExpirationMillis)
        settings.putLong(KEY_REFRESH_TOKEN_EXPIRATION, response.refreshTokenExpirationMillis)
        val me = authApi.getMe()
        settings.putString(KEY_USER_ID, me.id)
    }

    suspend fun createUserWithEmailAndPassword(email: String, userName: String, password: String): UserDetailsDto {
        return authApi.signup(UserRegistrationDto(email, userName, password))
    }

    suspend fun sendPasswordResetEmail(email: String) {
        authApi.sendPasswordResetEmail(PasswordResetRequest(email))
    }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun signOut() {
        settings.remove(KEY_AUTH_PROVIDER)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_ACCESS_TOKEN_EXPIRATION)
        settings.remove(KEY_REFRESH_TOKEN_EXPIRATION)
    }
}