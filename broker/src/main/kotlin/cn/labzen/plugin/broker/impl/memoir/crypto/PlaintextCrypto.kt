package cn.labzen.plugin.broker.impl.memoir.crypto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.Serializable

class PlaintextCrypto : Crypto {

  private val om = ObjectMapper().also {
    it.registerKotlinModule()
  }

  override fun <T : Serializable> encrypt(type: Class<T>, obj: T): String =
    om.writeValueAsString(obj)

  override fun <T : Serializable> decrypt(type: Class<T>, ciphertext: String): T =
    om.readValue(ciphertext, type)
}
