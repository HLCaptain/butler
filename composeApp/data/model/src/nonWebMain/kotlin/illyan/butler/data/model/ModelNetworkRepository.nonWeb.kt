package illyan.butler.data.model

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getDispatchersIO(): CoroutineDispatcher {
    return Dispatchers.IO
}
