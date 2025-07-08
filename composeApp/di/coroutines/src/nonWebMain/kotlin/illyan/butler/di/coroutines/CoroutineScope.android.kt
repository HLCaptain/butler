package illyan.butler.di.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getDispatcherIO(): CoroutineDispatcher {
    return Dispatchers.IO
}
