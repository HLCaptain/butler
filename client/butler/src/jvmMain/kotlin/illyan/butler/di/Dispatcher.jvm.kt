package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
@NamedCoroutineDispatcherIO
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO