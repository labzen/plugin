package cn.labzen.plugin.broker.memoir.crypto

import java.io.Serializable

interface Crypto {

  fun <T : Serializable> encrypt(type: Class<T>, obj: T): String

  fun <T : Serializable> decrypt(type: Class<T>, ciphertext: String): T
}
