package illyan.butler

import android.app.Application
import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.core.local.datastore.getDataStore
import illyan.butler.core.local.settings.createSettings
import illyan.butler.utils.initNapier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication : Application() {
    @OptIn(ExperimentalSettingsApi::class)
    override fun onCreate() {
        super.onCreate()

        initNapier()
        startKoin {
            androidLogger()
            androidContext(applicationContext)

            modules(
                module {
                    single { getDataStore(androidContext()) }
                    single { createSettings(androidContext()) }
                },
                *commonModules().toTypedArray()
            )
        }
    }
}
