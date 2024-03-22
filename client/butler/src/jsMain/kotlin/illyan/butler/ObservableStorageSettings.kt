package illyan.butler

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener

class ObservableStorageSettings(private val delegate: Settings): ObservableSettings {
    override val keys: Set<String>
        get() = delegate.keys
    override val size: Int
        get() = delegate.size

    private val booleanListeners = mutableMapOf<String, (Boolean) -> Unit>()
    override fun addBooleanListener(key: String, defaultValue: Boolean, callback: (Boolean) -> Unit): SettingsListener {
        booleanListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                booleanListeners.remove(key)
            }
        }
    }

    private val booleanOrNullListeners = mutableMapOf<String, (Boolean?) -> Unit>()
    override fun addBooleanOrNullListener(key: String, callback: (Boolean?) -> Unit): SettingsListener {
        booleanOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                booleanOrNullListeners.remove(key)
            }
        }
    }

    private val doubleListeners = mutableMapOf<String, (Double) -> Unit>()
    override fun addDoubleListener(key: String, defaultValue: Double, callback: (Double) -> Unit): SettingsListener {
        doubleListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                doubleListeners.remove(key)
            }
        }
    }

    private val doubleOrNullListeners = mutableMapOf<String, (Double?) -> Unit>()
    override fun addDoubleOrNullListener(key: String, callback: (Double?) -> Unit): SettingsListener {
        doubleOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                doubleOrNullListeners.remove(key)
            }
        }
    }

    private val floatListeners = mutableMapOf<String, (Float) -> Unit>()
    override fun addFloatListener(key: String, defaultValue: Float, callback: (Float) -> Unit): SettingsListener {
        floatListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                floatListeners.remove(key)
            }
        }
    }

    private val floatOrNullListeners = mutableMapOf<String, (Float?) -> Unit>()
    override fun addFloatOrNullListener(key: String, callback: (Float?) -> Unit): SettingsListener {
        floatOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                floatOrNullListeners.remove(key)
            }
        }
    }

    private val intListeners = mutableMapOf<String, (Int) -> Unit>()
    override fun addIntListener(key: String, defaultValue: Int, callback: (Int) -> Unit): SettingsListener {
        intListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                intListeners.remove(key)
            }
        }
    }

    private val intOrNullListeners = mutableMapOf<String, (Int?) -> Unit>()
    override fun addIntOrNullListener(key: String, callback: (Int?) -> Unit): SettingsListener {
        intOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                intOrNullListeners.remove(key)
            }
        }
    }

    private val longListeners = mutableMapOf<String, (Long) -> Unit>()
    override fun addLongListener(key: String, defaultValue: Long, callback: (Long) -> Unit): SettingsListener {
        longListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                longListeners.remove(key)
            }
        }
    }

    private val longOrNullListeners = mutableMapOf<String, (Long?) -> Unit>()
    override fun addLongOrNullListener(key: String, callback: (Long?) -> Unit): SettingsListener {
        longOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                longOrNullListeners.remove(key)
            }
        }
    }

    private val stringListeners = mutableMapOf<String, (String) -> Unit>()
    override fun addStringListener(key: String, defaultValue: String, callback: (String) -> Unit): SettingsListener {
        stringListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                stringListeners.remove(key)
            }
        }
    }

    private val stringOrNullListeners = mutableMapOf<String, (String?) -> Unit>()
    override fun addStringOrNullListener(key: String, callback: (String?) -> Unit): SettingsListener {
        stringOrNullListeners[key] = callback
        return object : SettingsListener {
            override fun deactivate() {
                stringOrNullListeners.remove(key)
            }
        }
    }

    override fun clear() {
        booleanOrNullListeners.values.forEach { it.invoke(null) }
        doubleOrNullListeners.values.forEach { it.invoke(null) }
        floatOrNullListeners.values.forEach { it.invoke(null) }
        intOrNullListeners.values.forEach { it.invoke(null) }
        longOrNullListeners.values.forEach { it.invoke(null) }
        stringOrNullListeners.values.forEach { it.invoke(null) }
        delegate.clear()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return delegate.getBoolean(key, defaultValue)
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return delegate.getBooleanOrNull(key)
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return delegate.getDouble(key, defaultValue)
    }

    override fun getDoubleOrNull(key: String): Double? {
        return delegate.getDoubleOrNull(key)
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return delegate.getFloat(key, defaultValue)
    }

    override fun getFloatOrNull(key: String): Float? {
        return delegate.getFloatOrNull(key)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return delegate.getInt(key, defaultValue)
    }

    override fun getIntOrNull(key: String): Int? {
        return delegate.getIntOrNull(key)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return delegate.getLong(key, defaultValue)
    }

    override fun getLongOrNull(key: String): Long? {
        return delegate.getLongOrNull(key)
    }

    override fun getString(key: String, defaultValue: String): String {
        return delegate.getString(key, defaultValue)
    }

    override fun getStringOrNull(key: String): String? {
        return delegate.getStringOrNull(key)
    }

    override fun hasKey(key: String): Boolean {
        return delegate.hasKey(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        booleanListeners[key]?.invoke(value)
        booleanOrNullListeners[key]?.invoke(value)
        delegate.putBoolean(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        doubleListeners[key]?.invoke(value)
        doubleOrNullListeners[key]?.invoke(value)
        delegate.putDouble(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        floatListeners[key]?.invoke(value)
        floatOrNullListeners[key]?.invoke(value)
        delegate.putFloat(key, value)
    }

    override fun putInt(key: String, value: Int) {
        intListeners[key]?.invoke(value)
        intOrNullListeners[key]?.invoke(value)
        delegate.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        longListeners[key]?.invoke(value)
        longOrNullListeners[key]?.invoke(value)
        delegate.putLong(key, value)
    }

    override fun putString(key: String, value: String) {
        stringListeners[key]?.invoke(value)
        stringOrNullListeners[key]?.invoke(value)
        delegate.putString(key, value)
    }

    override fun remove(key: String) {
        booleanOrNullListeners[key]?.invoke(null)
        doubleOrNullListeners[key]?.invoke(null)
        floatOrNullListeners[key]?.invoke(null)
        intOrNullListeners[key]?.invoke(null)
        longOrNullListeners[key]?.invoke(null)
        stringOrNullListeners[key]?.invoke(null)
        delegate.remove(key)
    }
}