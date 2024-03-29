package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named(KoinNames.DispatcherIO)
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO