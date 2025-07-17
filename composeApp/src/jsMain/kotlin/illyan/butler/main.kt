package illyan.butler

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.audio.AudioDomainModule
import illyan.butler.auth.AuthDomainModule
import illyan.butler.chat.ChatDomainModule
import illyan.butler.core.local.settings.createSettings
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
import illyan.butler.utils.initNapier
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSettingsApi::class)
fun main() {
    initNapier()
    startKoin {
        modules(
            module {
                single { createSettings() }
            },
            *commonModules().toTypedArray()
        )
    }

    onWasmReady {
        ComposeViewport("ComposeApp") {
            App()
        }
    }
}
