package illyan.butler.di.coroutines

import illyan.butler.data.error.ErrorRepository
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Factory
@Named(KoinNames.CoroutineScopeIOWithoutHandler)
fun provideCoroutineScopeIOWithoutHandler() = CoroutineScope(Dispatchers.IO)

@Factory
@Named(KoinNames.CoroutineScopeIO)
fun provideCoroutineScopeIO(exceptionHandler: CoroutineExceptionHandler) = CoroutineScope(Dispatchers.IO + exceptionHandler)

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
    coroutineScope.launch { errorRepository.reportError(throwable) }
}
