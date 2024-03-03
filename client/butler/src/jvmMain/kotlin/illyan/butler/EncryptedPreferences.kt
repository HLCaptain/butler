package illyan.butler

import io.github.aakira.napier.Napier
import java.io.OutputStream
import java.security.Key
import java.security.SecureRandom
import java.util.prefs.NodeChangeListener
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.asKotlinRandom

class EncryptedPreferences(
    key: Key,
    // New node needed to avoid interacting with other non-encrypted preferences
    private val delegate: Preferences = userRoot().node("illyan.butler"),
    javaRandom: SecureRandom = SecureRandom()
): Preferences() {
    private val random = javaRandom.asKotlinRandom()
    private val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")

    init {
        // 128 is the tag length, 16 is the iv length
        val iv = random.nextBytes(16)
        val gcmParameterSpec = GCMParameterSpec(128, iv, 0, 16)
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)
        decryptCipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)
    }

    private fun encrypt(value: String?): String? {
        return value?.let {
            Napier.i { "Encrypting: $it" }
            encryptCipher.update(it.toByteArray())
            encryptCipher.doFinal(it.toByteArray()).toString(Charsets.UTF_8)
        }
    }

    private fun decrypt(value: String?): String? {
        return value?.let {
            Napier.i { "Decrypting: $it" }
            decryptCipher.update(it.toByteArray())
            decryptCipher.doFinal(it.toByteArray()).toString(Charsets.UTF_8)
        }
    }

    override fun toString(): String {
        return "EncryptedPreferences"
    }

    override fun put(p0: String?, p1: String?) {
        delegate.put(encrypt(p0), encrypt(p1))
    }

    override fun get(p0: String?, p1: String?): String? {
        return decrypt(delegate.get(encrypt(p0), encrypt(p1)))
    }

    override fun remove(p0: String?) {
        delegate.remove(encrypt(p0))
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
        return delegate.keys().map { decrypt(it) }.toTypedArray()
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
                val decryptedKey = decrypt(event.key)
                val decryptedNewValue = decrypt(event.newValue)
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