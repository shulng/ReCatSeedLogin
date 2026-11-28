package cc.baka9.catseedlogin.common.util

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Arrays
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecretCodec {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_BITS = 256
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val KEY_FILE_NAME = "2fa.key"
    private val RANDOM = SecureRandom()
    @Volatile private var key: SecretKey? = null

    @JvmStatic @Synchronized @Throws(IOException::class)
    fun init(dataFolder: File?) {
        if (key != null) return
        if (dataFolder == null) throw IOException("插件数据目录为空，无法初始化 $KEY_FILE_NAME")
        if (!dataFolder.exists() && !dataFolder.mkdirs()) throw IOException("无法创建插件数据目录: $dataFolder")
        val file = File(dataFolder, KEY_FILE_NAME)
        val loaded = if (file.isFile) readKey(file) else null
        if (file.isFile && loaded == null) throw IOException("2FA 密钥文件损坏或格式无效: $file")
        var initialized = loaded
        if (initialized == null) {
            initialized = generateKey()
            Files.write(file.toPath(), Base64.getEncoder().encodeToString(initialized.encoded).toByteArray(StandardCharsets.UTF_8))
        }
        key = initialized
    }

    @JvmStatic fun isReady(): Boolean = key != null

    @JvmStatic @Throws(GeneralSecurityException::class)
    fun encrypt(plain: String): String {
        val secretKey = key ?: error("SecretCodec 尚未初始化")
        val iv = ByteArray(IV_BYTES).also(RANDOM::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_BITS, iv))
        val cipherText = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
        val out = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(cipherText, 0, out, iv.size, cipherText.size)
        return Base64.getEncoder().encodeToString(out)
    }

    @JvmStatic fun decrypt(stored: String?): String? {
        val secretKey = key ?: return null
        if (stored.isNullOrEmpty()) return null
        return try {
            val all = Base64.getDecoder().decode(stored)
            if (all.size <= IV_BYTES) return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_BITS, Arrays.copyOf(all, IV_BYTES)))
            String(cipher.doFinal(Arrays.copyOfRange(all, IV_BYTES, all.size)), StandardCharsets.UTF_8)
        } catch (_: Exception) { null }
    }

    private fun generateKey(): SecretKey = try {
        KeyGenerator.getInstance(ALGORITHM).apply { init(KEY_BITS, RANDOM) }.generateKey()
    } catch (e: GeneralSecurityException) { throw IllegalStateException("无法生成 AES 密钥", e) }

    private fun readKey(file: File): SecretKey? = try {
        val decoded = Base64.getDecoder().decode(String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim())
        if (decoded.size != KEY_BITS / 8) null else SecretKeySpec(decoded, ALGORITHM)
    } catch (_: Exception) { null }
}
