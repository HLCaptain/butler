package illyan.butler

import android.app.Application
import illyan.butler.audio.AudioDomainModule
import illyan.butler.auth.AuthDomainModule
import illyan.butler.chat.ChatDomainModule
import illyan.butler.config.ConfigDomainModule
import illyan.butler.core.local.room.RoomCoreModule
import illyan.butler.core.network.ktor.KtorCoreModule
import illyan.butler.data.chat.ChatDataModule
import illyan.butler.data.error.ErrorDataModule
import illyan.butler.data.host.HostDataModule
import illyan.butler.data.message.MessageDataModule
import illyan.butler.data.model.ModelDataModule
import illyan.butler.data.resource.ResourceDataModule
import illyan.butler.data.settings.SettingsDataModule
import illyan.butler.data.user.UserDataModule
import illyan.butler.di.RepositoryModule
import illyan.butler.error.ErrorManager
import illyan.butler.host.HostDomainModule
import illyan.butler.model.ModelManager
import illyan.butler.settings.SettingsDomainModule
import illyan.butler.ui.AuthFeatureModule
import illyan.butler.ui.ChatFeatureModule
import illyan.butler.ui.OnboardingFeatureModule
import illyan.butler.ui.ProfileFeatureModule
import illyan.butler.ui.error.ErrorFeatureModule
import illyan.butler.ui.home.HomeFeatureModule
import illyan.butler.ui.permission.PermissionFeatureModule
import illyan.butler.ui.theme.ThemeFeatureModule
import illyan.butler.utils.initNapier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initNapier()
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            val featureModules = listOf(
                AuthFeatureModule().module,
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
                SettingsDomainModule().module
            )
            val dataModules = listOf(
                ChatDataModule().module,
                HostDataModule().module,
                MessageDataModule().module,
                ModelDataModule().module,
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
    }
}