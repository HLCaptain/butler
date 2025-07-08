package illyan.butler.core.local.settings

import android.content.Context
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import illyan.butler.core.local.datastore.getDataStore
import org.koin.core.annotation.Single

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
@Single
fun createSettings(context: Context): FlowSettings {
    return DataStoreSettings(
        datastore = getDataStore(context),
    )
}
