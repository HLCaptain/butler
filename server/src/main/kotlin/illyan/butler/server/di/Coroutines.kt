package illyan.butler.server.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

@Single
fun provideDispatcherIO() = Dispatchers.IO

@Factory
fun provideCoroutineScopeIO() = CoroutineScope(provideDispatcherIO())
