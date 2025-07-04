package illyan.butler.di.coroutines

import illyan.butler.data.error.ErrorRepository
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

expect fun getDispatcherIO(): CoroutineDispatcher

@Factory
@Named(KoinNames.CoroutineScopeIOWithoutHandler)
fun provideCoroutineScopeIOWithoutHandler() = CoroutineScope(getDispatcherIO())

@Factory
@Named(KoinNames.CoroutineScopeIO)
fun provideCoroutineScopeIO(exceptionHandler: CoroutineExceptionHandler) = CoroutineScope(getDispatcherIO() + exceptionHandler)

@Factory
@Named(KoinNames.CoroutineScopeMain)
fun provideCoroutineScopeMain(exceptionHandler: CoroutineExceptionHandler) = CoroutineScope(Dispatchers.Main + exceptionHandler)

@Factory
@Named(KoinNames.CoroutineScopeDefault)
fun provideCoroutineScopeDefault(exceptionHandler: CoroutineExceptionHandler) = CoroutineScope(Dispatchers.Default + exceptionHandler)

@Single
fun provideCoroutineExceptionHandler(
    errorRepository: ErrorRepository,
    @Named(KoinNames.CoroutineScopeIOWithoutHandler) coroutineScope: CoroutineScope
) = CoroutineExceptionHandler { _, throwable ->
    Napier.v { "CoroutineExceptionHandler caught: $throwable" }
    throwable.printStackTrace()
    coroutineScope.launch { errorRepository.reportError(throwable) }
}
