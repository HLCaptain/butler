package illyan.butler.data.model

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual fun getDispatchersIO(): CoroutineDispatcher {
    return Dispatchers.IO
}
