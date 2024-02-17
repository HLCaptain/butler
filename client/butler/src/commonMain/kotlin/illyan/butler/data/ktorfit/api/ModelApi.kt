package illyan.butler.data.ktorfit.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import illyan.butler.data.network.model.ModelDto

interface ModelApi {
    @GET("/models/{uuid}")
    suspend fun fetchModel(@Path("uuid") uuid: String): ModelDto

    @GET("/models")
    suspend fun fetchAllModels(): List<ModelDto>
}