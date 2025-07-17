package illyan.butler.di.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual fun getDispatcherIO(): CoroutineDispatcher {
    return Dispatchers.IO
}
