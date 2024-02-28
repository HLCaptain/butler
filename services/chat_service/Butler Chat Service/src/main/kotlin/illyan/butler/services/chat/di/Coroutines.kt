package illyan.butler.services.chat.di

import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
fun provideDispatcher() = Dispatchers.IO