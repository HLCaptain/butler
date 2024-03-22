package illyan.butler

import java.io.OutputStream
import java.security.Key
import java.security.SecureRandom
import java.util.prefs.NodeChangeListener
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.asKotlinRandom

class EncryptedPreferences(
    private val secretKey: Key,
    // New node needed to avoid interacting with other non-encrypted preferences
    private val delegate: Preferences = userRoot().node("illyan.butler"),
    javaRandom: SecureRandom = SecureRandom()
): Preferences() {
    companion object {
        private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12 // Size for GCM
        private const val TAG_LENGTH_BIT = 128 // Recommended tag length for GCM
    }

    private val random = javaRandom.asKotlinRandom()

    @OptIn(ExperimentalEncodingApi::class)
    private fun decryptWithIV(encryptedData: String?, ivSize: Int = IV_SIZE_BYTES): String? {
        return encryptedData?.let {
            val rawData = Base64.decode(it)
            val iv = rawData.copyOfRange(0, ivSize) // Extract IV from value
            val encryptedValue = rawData.copyOfRange(ivSize, rawData.size)

            val cipher = Cipher.getInstance(AES_GCM_NOPADDING).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))
            }

            cipher.doFinal(encryptedValue).decodeToString()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptWithIV(value: String?, iv: ByteArray = random.nextBytes(IV_SIZE_BYTES)): String? {
        return value?.let {
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BIT, iv))

            val encryptedValue = cipher.doFinal(it.toByteArray())

            // Add IV to the value to retrieve it later
            Base64.encode(iv + encryptedValue)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encryptWithHashedIV(value: String?): String? {
        // IV derived from the encrypted data
        val iv = value?.let { Base64.encode(value.toByteArray()).toByteArray().copyOfRange(0, IV_SIZE_BYTES) }
        return iv?.let { encryptWithIV(value, it) }
    }

    private fun putEncrypted(key: String?, value: String?) {
        delegate.put(encryptWithHashedIV(key), encryptWithIV(value))
    }

    private fun getDecrypted(key: String?): String? {
        return decryptWithIV(delegate.get(encryptWithHashedIV(key), null))
    }

    override fun toString(): String {
        return "EncryptedPreferences"
    }

    override fun put(p0: String?, p1: String?) {
        putEncrypted(p0, p1)
    }

    override fun get(p0: String?, p1: String?): String? {
        // Might sometimes throw an error for the key being null.
        // Not found definitive reason for this exception.
        return getDecrypted(p0) ?: p1
    }

    override fun remove(p0: String?) {
        delegate.remove(encryptWithHashedIV(p0))
    }

    override fun clear() {
        delegate.clear()
    }

    override fun putInt(p0: String?, p1: Int) {
        put(p0, p1.toString())
    }

    override fun getInt(p0: String?, p1: Int): Int {
        return get(p0, null)?.toInt() ?: p1
    }

    override fun putLong(p0: String?, p1: Long) {
        put(p0, p1.toString())
    }

    override fun getLong(p0: String?, p1: Long): Long {
        return get(p0, null)?.toLong() ?: p1
    }

    override fun putBoolean(p0: String?, p1: Boolean) {
        put(p0, p1.toString())
    }

    override fun getBoolean(p0: String?, p1: Boolean): Boolean {
        return get(p0, p1.toString()).toBoolean()
    }

    override fun putFloat(p0: String?, p1: Float) {
        put(p0, p1.toString())
    }

    override fun getFloat(p0: String?, p1: Float): Float {
        return get(p0, null)?.toFloat() ?: p1
    }

    override fun putDouble(p0: String?, p1: Double) {
        put(p0, p1.toString())
    }

    override fun getDouble(p0: String?, p1: Double): Double {
        return get(p0, null)?.toDouble() ?: p1
    }

    override fun putByteArray(p0: String?, p1: ByteArray?) {
        put(p0, p1.toString())
    }

    override fun getByteArray(p0: String?, p1: ByteArray?): ByteArray? {
        return get(p0, null)?.toByteArray() ?: p1
    }

    override fun keys(): Array<String?> {
        return delegate.keys().map { decryptWithIV(it) }.toTypedArray()
    }

    override fun childrenNames(): Array<String> {
        return delegate.childrenNames()
    }

    override fun parent(): Preferences {
        return delegate.parent()
    }

    override fun node(p0: String?): Preferences {
        return delegate.node(p0)
    }

    override fun nodeExists(p0: String?): Boolean {
        return delegate.nodeExists(p0)
    }

    override fun removeNode() {
        delegate.removeNode()
    }

    override fun name(): String {
        return delegate.name()
    }

    override fun absolutePath(): String {
        return delegate.absolutePath()
    }

    override fun isUserNode(): Boolean {
        return delegate.isUserNode
    }

    override fun flush() {
        delegate.flush()
    }

    override fun sync() {
        delegate.sync()
    }

    private val preferenceChangeListeners = mutableMapOf<PreferenceChangeListener, PreferenceChangeListener>()
    override fun addPreferenceChangeListener(p0: PreferenceChangeListener?) {
        p0?.let {
            val listener = PreferenceChangeListener { event ->
                val decryptedKey = decryptWithIV(event.key)
                val decryptedNewValue = decryptWithIV(event.newValue)
                p0.preferenceChange(PreferenceChangeEvent(this, decryptedKey, decryptedNewValue))
            }
            preferenceChangeListeners[it] = listener
            delegate.addPreferenceChangeListener(listener)
        }
    }

    override fun removePreferenceChangeListener(p0: PreferenceChangeListener?) {
        p0?.let {
            val listener = preferenceChangeListeners.remove(p0)
            listener?.let { delegate.removePreferenceChangeListener(it) }
        }
    }

    override fun addNodeChangeListener(p0: NodeChangeListener?) {
        delegate.addNodeChangeListener(p0)
    }

    override fun removeNodeChangeListener(p0: NodeChangeListener?) {
        delegate.removeNodeChangeListener(p0)
    }

    override fun exportNode(p0: OutputStream?) {
        delegate.exportNode(p0)
    }

    override fun exportSubtree(p0: OutputStream?) {
        delegate.exportSubtree(p0)
    }
}