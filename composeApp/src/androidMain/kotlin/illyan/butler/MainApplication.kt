package illyan.butler

import android.app.Application
import illyan.butler.data.permission.AndroidPermissionRepository
import illyan.butler.di.getViewModelModule
import illyan.butler.utils.initNapier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initNapier()
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(defaultModule, getViewModelModule(), module {
                single { AndroidPermissionRepository() }
            })
        }
    }
}