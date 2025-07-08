package illyan.butler.core.local.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import illyan.butler.core.local.datastore.getDataStore

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
fun createSettings(): FlowSettings {
    return DataStoreSettings(datastore = getDataStore())
}
