package illyan.butler.core.local.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.core.local.sqldelight.mapping.toDomainModel
import illyan.butler.core.local.sqldelight.mapping.toSqlDelightModel
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class CredentialSqlDelightDataSource(
    private val database: ButlerDatabase
) : CredentialLocalDataSource {

    override fun getCredentials(): Flow<List<ApiKeyCredential>> {
        Napier.d { "Getting all credentials" }
        return database.credentialQueries.selectAllCredentials()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { credentials -> credentials.map { it.toDomainModel() } }
    }

    override suspend fun upsertCredential(credential: ApiKeyCredential) {
        Napier.d { "Upserting credential for provider: ${'$'}{credential.providerUrl}" }
        val sqlDelightCredential = credential.toSqlDelightModel()
        database.credentialQueries.insertOrReplaceCredential(
            providerUrl = sqlDelightCredential.providerUrl,
            apiKey = sqlDelightCredential.apiKey
        )
    }

    override suspend fun deleteCredential(providerUrl: String) {
        Napier.d { "Deleting credential for provider: $providerUrl" }
        database.credentialQueries.deleteCredential(providerUrl)
    }

    override suspend fun deleteAllCredentials() {
        Napier.d { "Deleting all credentials" }
        database.credentialQueries.deleteAllCredentials()
    }

    override fun getUserTokens(userId: Uuid): Flow<UserTokens?> {
        Napier.d { "Getting user tokens for user: $userId" }
        return database.credentialQueries.selectUserTokens(userId.toString())
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainModel() }
    }

    override suspend fun upsertUserTokens(userId: Uuid, tokens: UserTokens) {
        Napier.d { "Upserting user tokens for user: $userId" }
        val sqlDelightTokens = tokens.toSqlDelightModel(userId)
        database.credentialQueries.insertOrReplaceUserTokens(
            userId = sqlDelightTokens.userId,
            accessToken = sqlDelightTokens.accessToken,
            refreshToken = sqlDelightTokens.refreshToken,
            accessTokenExpirationTime = sqlDelightTokens.accessTokenExpirationTime,
            refreshTokenExpirationTime = sqlDelightTokens.refreshTokenExpirationTime,
        )
    }
}
