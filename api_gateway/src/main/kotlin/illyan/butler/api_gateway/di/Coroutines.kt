package illyan.butler.api_gateway.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
fun provideDispatcherIO() = Dispatchers.IO

@Factory
fun provideCoroutineScopeIO() = CoroutineScope(provideDispatcherIO())
