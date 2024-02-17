package illyan.butler.data.network.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import illyan.butler.data.network.model.auth.AuthCredentials
import illyan.butler.data.network.model.auth.AuthResponse
import illyan.butler.data.network.model.auth.PasswordResetRequest

interface AuthApi {
    @POST("/auth/signin")
    suspend fun signIn(@Body credentials: AuthCredentials): AuthResponse

    @POST("/auth/signup")
    suspend fun signUp(@Body credentials: AuthCredentials): AuthResponse

    @POST("/auth/reset-password")
    suspend fun sendPasswordResetEmail(@Body request: PasswordResetRequest)

    @GET("/auth/signout")
    suspend fun signOut(): Response<Unit>
}