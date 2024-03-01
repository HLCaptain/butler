package illyan.butler.di

import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

@Single
expect fun provideSettings(): Settings