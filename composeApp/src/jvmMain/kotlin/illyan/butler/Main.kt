package illyan.butler

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import illyan.butler.audio.AudioDomainModule
import illyan.butler.auth.AuthDomainModule
import illyan.butler.chat.ChatDomainModule
import illyan.butler.config.ConfigDomainModule
import illyan.butler.core.local.room.RoomCoreModule
import illyan.butler.core.network.ktor.KtorCoreModule
import illyan.butler.data.chat.ChatDataModule
import illyan.butler.data.error.ErrorDataModule
import illyan.butler.data.host.HostDataModule
import illyan.butler.data.permission.PermissionDataModule
import illyan.butler.data.message.MessageDataModule
import illyan.butler.data.model.ModelDataModule
import illyan.butler.data.resource.ResourceDataModule
import illyan.butler.data.settings.SettingsDataModule
import illyan.butler.data.user.UserDataModule
import illyan.butler.di.repository.RepositoryModule
import illyan.butler.error.ErrorManager
import illyan.butler.model.ModelManager
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.app_name
import illyan.butler.generated.resources.butler_logo
import illyan.butler.host.HostDomainModule
import illyan.butler.permission.PermissionDomainModule
import illyan.butler.settings.SettingsDomainModule
import illyan.butler.ui.AuthFeatureModule
import illyan.butler.ui.ChatFeatureModule
import illyan.butler.ui.ErrorFeatureModule
import illyan.butler.ui.HomeFeatureModule
import illyan.butler.ui.OnboardingFeatureModule
import illyan.butler.ui.PermissionFeatureModule
import illyan.butler.ui.ProfileFeatureModule
import illyan.butler.ui.ThemeFeatureModule
import illyan.butler.utils.initNapier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.module

fun main() = application {
    initNapier()
    startKoin {
        val featureModules = listOf(AuthFeatureModule().module,
            ChatFeatureModule().module,
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
            ConfigDomainModule().module,
            HostDomainModule().module,
            module { singleOf(::ModelManager) },
            PermissionDomainModule().module,
            SettingsDomainModule().module
        )
        val dataModules = listOf(
            ChatDataModule().module,
            HostDataModule().module,
            MessageDataModule().module,
            ModelDataModule().module,
            PermissionDataModule().module,
            ResourceDataModule().module,
            SettingsDataModule().module,
            UserDataModule().module
        )
        val coreModules = listOf(
            ErrorDataModule().module,
            module { singleOf(::ErrorManager) },
            RoomCoreModule().module,
            KtorCoreModule().module
        )
        modules(
            *coreModules.toTypedArray(),
            *dataModules.toTypedArray(),
            RepositoryModule().module,
            *domainModules.toTypedArray(),
            *featureModules.toTypedArray()
        )
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.butler_logo)
    ) {
        App()
    }
}
