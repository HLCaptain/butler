package illyan.butler.core.local.sql.datasource

import illyan.butler.core.local.datasource.CredentialLocalDataSource
import illyan.butler.core.local.sql.DatabaseHelper
import illyan.butler.core.local.sql.mapping.toDomainModel
import illyan.butler.core.local.sql.mapping.toSqlDelightModel
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.domain.model.UserTokens
import illyan.butler.shared.model.auth.ApiKeyCredential
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class CredentialSqlDelightDataSource(
    private val databaseHelper: DatabaseHelper<ButlerDatabase>
) : CredentialLocalDataSource {

    override fun getCredentials(): Flow<List<ApiKeyCredential>> {
        Napier.d { "Getting all credentials" }
        return databaseHelper.queryAsListFlow {
            credentialQueries.selectAllCredentials()
        }.map { credentials ->
            credentials.map { it.toDomainModel() } 
        }
    }

    override suspend fun upsertCredential(credential: ApiKeyCredential) {
        Napier.d { "Upserting credential for provider: ${credential.providerUrl}" }
        val sqlDelightCredential = credential.toSqlDelightModel()
        databaseHelper.withDatabase {
            credentialQueries.insertOrReplaceCredential(
                providerUrl = sqlDelightCredential.providerUrl,
                apiKey = sqlDelightCredential.apiKey
            )
        }
    }

    override suspend fun deleteCredential(providerUrl: String) {
        Napier.d { "Deleting credential for provider: $providerUrl" }
        databaseHelper.withDatabase {
            credentialQueries.deleteCredential(providerUrl)
        }
    }

    override suspend fun deleteAllCredentials() {
        Napier.d { "Deleting all credentials" }
        databaseHelper.withDatabase {
            credentialQueries.deleteAllCredentials()
        }
    }

    override fun getUserTokens(userId: Uuid): Flow<UserTokens?> {
        Napier.d { "Getting user tokens for user: $userId" }
        return databaseHelper.queryAsOneOrNullFlow {
            credentialQueries.selectUserTokens(userId.toString())
        }.map { it?.toDomainModel() }
    }

    override suspend fun upsertUserTokens(userId: Uuid, tokens: UserTokens) {
        Napier.d { "Upserting user tokens for user: $userId" }
        val sqlDelightTokens = tokens.toSqlDelightModel(userId)
        databaseHelper.withDatabase {
            credentialQueries.insertOrReplaceUserTokens(
                userId = sqlDelightTokens.userId,
                accessToken = sqlDelightTokens.accessToken,
                refreshToken = sqlDelightTokens.refreshToken,
                accessTokenExpirationTime = sqlDelightTokens.accessTokenExpirationTime,
                refreshTokenExpirationTime = sqlDelightTokens.refreshTokenExpirationTime,
            )
        }
    }
}
