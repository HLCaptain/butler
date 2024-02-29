package illyan.butler.data.ktorfit.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import illyan.butler.data.network.model.auth.AuthResponse
import illyan.butler.data.network.model.auth.PasswordResetRequest
import illyan.butler.data.network.model.auth.UserLoginDto

interface AuthApi {
    @POST("/signup")
    suspend fun signup(@Body credentials: UserLoginDto): AuthResponse

    @POST("/login")
    suspend fun login(@Body credentials: UserLoginDto): AuthResponse

    @POST("/reset-password")
    suspend fun sendPasswordResetEmail(@Body request: PasswordResetRequest)

    @GET("/me")
    suspend fun getMe(): Response<Unit>

    // Signout is not implemented with JWT
//    @GET("/signout")
//    suspend fun signOut(): Response<Unit>
}