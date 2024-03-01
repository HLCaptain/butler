package illyan.butler.data.ktorfit.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto
import illyan.butler.data.network.model.auth.UserRegistrationDto
import illyan.butler.data.network.model.auth.UserTokensResponse
import illyan.butler.data.network.model.identity.UserDetailsDto

interface AuthApi {
    @POST("/signup")
    suspend fun signup(@Body credentials: UserRegistrationDto): UserDetailsDto

    @POST("/login")
    suspend fun login(@Body credentials: UserLoginDto): UserTokensResponse

    @POST("/reset-password")
    suspend fun sendPasswordResetEmail(@Body request: PasswordResetRequest)

    @GET("/me")
    suspend fun getMe(): UserDetailsDto

    // Websocket to /me


    // Signout is not implemented with JWT
//    @GET("/signout")
//    suspend fun signOut(): Response<Unit>
}