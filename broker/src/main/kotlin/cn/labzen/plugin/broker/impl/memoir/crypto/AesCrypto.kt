package cn.labzen.plugin.broker.impl.memoir.crypto

import java.io.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

class AesCrypto(secretKeyString: String) : Crypto {

  private val secretKeySpec: SecretKeySpec
  private val cipher: Cipher

  init {
    // Generate a secret key for AES algorithm
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(128)
    // val secretKey = keyGen.generateKey()

    // Convert the key to byte array and wrap it in a SecretKeySpec object
    val keyBytes = secretKeyString.toByteArray()
    val paddedKeyBytes = ByteArray(32)
    System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, min(keyBytes.size, paddedKeyBytes.size))
    secretKeySpec = SecretKeySpec(paddedKeyBytes, "AES")

    // Create a cipher object and initialize it for encryption
    cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
  }

  override fun <T : Serializable> encrypt(type: Class<T>, obj: T): String {
    // 序列化
    val bytes = ByteArrayOutputStream().use { bos ->
      ObjectOutputStream(bos).use { oos ->
        oos.writeObject(obj)
        bos.toByteArray()
      }
    }

    // Encrypt the plain text
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    val encryptedBytes = cipher.doFinal(bytes)

    return Base64.getEncoder().encodeToString(encryptedBytes)
  }

  override fun <T : Serializable> decrypt(type: Class<T>, ciphertext: String): T {
    val bytes = Base64.getDecoder().decode(ciphertext)

    // Decrypt the encrypted text
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
    val decryptedBytes = cipher.doFinal(bytes)

    return ByteArrayInputStream(decryptedBytes).use { bis ->
      ObjectInputStream(bis).use { ois ->
        @Suppress("UNCHECKED_CAST")
        ois.readObject() as T
      }
    }
  }
}
