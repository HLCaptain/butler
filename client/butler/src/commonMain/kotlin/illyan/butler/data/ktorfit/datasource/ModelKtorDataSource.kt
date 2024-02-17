package illyan.butler.data.ktorfit.datasource

import illyan.butler.data.ktorfit.api.ModelApi
import illyan.butler.data.network.datasource.ModelNetworkDataSource
import illyan.butler.data.network.model.ModelDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ModelKtorDataSource(
    private val modelApi: ModelApi
) : ModelNetworkDataSource {

    override fun fetch(uuid: String): Flow<ModelDto> = flow {
        emit(modelApi.fetchModel(uuid))
    }.catch { exception ->
        Napier.e("Error fetching model", exception)
    }

    override fun fetchAll(): Flow<List<ModelDto>> = flow {
        emit(modelApi.fetchAllModels())
    }.catch { exception ->
        Napier.e("Error fetching all models", exception)
    }
}