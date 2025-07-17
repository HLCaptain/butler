package illyan.butler

import illyan.butler.audio.AudioDomainModule
import illyan.butler.auth.AuthDomainModule
import illyan.butler.chat.ChatDomainModule
import illyan.butler.core.local.sql.SqlCoreModule
import illyan.butler.core.network.ktor.KtorCoreModule
import illyan.butler.data.chat.ChatDataModule
import illyan.butler.data.credential.CredentialDataModule
import illyan.butler.data.error.ErrorDataModule
import illyan.butler.data.host.HostDataModule
import illyan.butler.data.message.MessageDataModule
import illyan.butler.data.model.ModelDataModule
import illyan.butler.data.resource.ResourceDataModule
import illyan.butler.data.settings.SettingsDataModule
import illyan.butler.data.user.UserDataModule
import illyan.butler.di.RepositoryModule
import illyan.butler.di.coroutines.CoroutineModule
import illyan.butler.di.datasource.DataSourceModule
import illyan.butler.host.HostDomainModule
import illyan.butler.model.ModelDomainModule
import illyan.butler.settings.SettingsDomainModule
import illyan.butler.ui.AuthFeatureModule
import illyan.butler.ui.ChatFeatureModule
import illyan.butler.ui.DashboardFeatureModule
import illyan.butler.ui.OnboardingFeatureModule
import illyan.butler.ui.error.ErrorFeatureModule
import illyan.butler.ui.home.HomeFeatureModule
import illyan.butler.ui.permission.PermissionFeatureModule
import illyan.butler.ui.profile.ProfileFeatureModule
import illyan.butler.ui.theme.ThemeFeatureModule
import org.koin.core.module.Module
import org.koin.ksp.generated.module

expect fun isDebugBuild(): Boolean

fun commonModules(): List<Module> {
    val featureModules = listOf(
        AuthFeatureModule().module,
        ChatFeatureModule().module,
        DashboardFeatureModule().module,
        ErrorFeatureModule().module,
        HomeFeatureModule().module,
        OnboardingFeatureModule().module,
        PermissionFeatureModule().module,
        ProfileFeatureModule().module,
        ThemeFeatureModule().module
    )
    val domainModules = listOf(
        AudioDomainModule().module,
        AuthDomainModule().module,
        ChatDomainModule().module,
        HostDomainModule().module,
        ModelDomainModule().module,
        SettingsDomainModule().module
    )
    val dataModules = listOf(
        ChatDataModule().module,
        CredentialDataModule().module,
        HostDataModule().module,
        MessageDataModule().module,
        ModelDataModule().module,
        ResourceDataModule().module,
        SettingsDataModule().module,
        UserDataModule().module
    )
    val coreModules = listOf(
        SqlCoreModule().module,
        KtorCoreModule().module,
        DataSourceModule().module
    )
    return listOf(
        ErrorDataModule().module,
        CoroutineModule().module, // Must be after ErrorDataModule
        *coreModules.toTypedArray(),
        *dataModules.toTypedArray(),
        RepositoryModule().module,
        *domainModules.toTypedArray(),
        *featureModules.toTypedArray()
    )
}
