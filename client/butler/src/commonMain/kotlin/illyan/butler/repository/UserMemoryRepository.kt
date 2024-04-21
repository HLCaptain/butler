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
import illyan.butler.util.log.randomUUID
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

@OptIn(ExperimentalSettingsApi::class)
@Single
class UserMemoryRepository(
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScope: CoroutineScope
) : UserRepository {
    private val _isUserSigningIn = MutableStateFlow(false)

    private val _userData = MutableStateFlow<UserDto?>(null)
    override val userData = _userData.asStateFlow()
    override val isUserSignedIn = userData.map { it != null }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserId = userData.map { it?.id }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserEmail = userData.map { it?.email }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhoneNumber = userData.map { it?.phone }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserPhotoURL = userData.map { it?.photoUrl }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val signedInUserName = userData.map { it?.username }.stateIn(coroutineScope, SharingStarted.Eagerly, null)
    override val isUserSigningIn = _isUserSigningIn.asStateFlow()

    override suspend fun loginWithEmailAndPassword(email: String, password: String) {
        _userData.update {
            UserDto(
                id = randomUUID(),
                email = email,
                phone = null,
                photoUrl = null,
                username = email,
                displayName = email,
                fullName = email,
                address = null,
            )
        }
    }

    override suspend fun signUpAndLogin(email: String, password: String, userName: String) {
        _userData.update {
            UserDto(
                id = randomUUID(),
                email = email,
                phone = null,
                photoUrl = null,
                username = userName,
                displayName = userName,
                fullName = userName,
                address = null,
            )
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        // This function would need to be modified or removed as it depends on the AuthNetworkDataSource
    }

    override suspend fun signOut() {
        _userData.update { null }
    }

    override suspend fun deleteUserData() {
        _userData.update { null }
    }
}