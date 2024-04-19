package illyan.butler.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import illyan.butler.data.network.datasource.AuthNetworkDataSource
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserLoginResponseDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.identity.UserDto
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface UserRepository {
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_AUTH_PROVIDER = "auth_provider"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_ACCESS_TOKEN_EXPIRATION = "access_token_expiration"
        const val KEY_REFRESH_TOKEN_EXPIRATION = "refresh_token_expiration"
        const val FIRST_SIGN_IN_HAPPENED_YET = "FIRST_SIGN_IN_HAPPENED_YET"
    }

    val userData: StateFlow<UserDto?>
    val isUserSignedIn: StateFlow<Boolean?>
    val signedInUserUUID: StateFlow<String?>
    val signedInUserEmail: StateFlow<String?>
    val signedInUserPhoneNumber: StateFlow<String?>
    val signedInUserPhotoURL: StateFlow<String?>
    val signedInUserName: StateFlow<String?>
    val isUserSigningIn: StateFlow<Boolean>

    suspend fun loginWithEmailAndPassword(email: String, password: String)
    suspend fun signUpAndLogin(email: String, password: String, userName: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun signOut()
    suspend fun deleteUserData()
}
