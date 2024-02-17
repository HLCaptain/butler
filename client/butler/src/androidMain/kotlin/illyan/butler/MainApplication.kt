package illyan.butler

import android.app.Application
import illyan.butler.util.log.initNapier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initNapier(BuildConfig.DEBUG)

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            defaultModule()
        }
    }
}