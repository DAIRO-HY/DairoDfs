package cn.dairo.lib.server

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @author Long
 */
object AESUtil {
    @JvmStatic
    fun main(args: Array<String>) {
        val en = encrypt("22|1")
        println(en)
        println(decrypt(en))
    }

    const val DEFAULT_KEY = "je98jhdj983ufjoa"

    fun decrypt(content: String?, key: String = DEFAULT_KEY): String? {
        var result: String? = null
        var decryptResult: ByteArray? = null
        try {
            val decoder = Base64.getDecoder()
            val contentBytes = decoder.decode(content)
            //            byte[] contentBytes = new BASE64Decoder().decodeBuffer(content);
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            decryptResult = cipher.doFinal(contentBytes)
            if (decryptResult != null) {
                result = String(decryptResult, charset("UTF-8"))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return result
    }

    fun encrypt(content: String, key: String = DEFAULT_KEY): String? {
        var encryptResult: ByteArray? = null
        var result: String? = null
        try {
            val contentBytes = content.toByteArray(charset("UTF-8"))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
            encryptResult = cipher.doFinal(contentBytes)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (encryptResult != null) {
            val encoder = Base64.getEncoder()
            result = encoder.encodeToString(encryptResult)
            //            result = new BASE64Encoder().encode(encryptResult);
        }
        return result
    }
}