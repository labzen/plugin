package cn.labzen.plugin.broker.impl.memoir

import cn.labzen.plugin.broker.impl.memoir.bean.MemoirContext
import cn.labzen.plugin.broker.impl.memoir.crypto.Crypto
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*

internal object ContextAccessor {

  private lateinit var crypto: Crypto
  fun setCrypto(crypto: Crypto) {
    ContextAccessor.crypto = crypto
  }

  /**
   * 记录插件的 Context 信息
   */
  fun record(path: Path, context: MemoirContext) {
    val content = crypto.encrypt(MemoirContext::class.java, context)
    try {
      Files.writeString(path, content, CREATE, WRITE, TRUNCATE_EXISTING)
    } catch (e: Exception) {
      // log error
    }
  }

  fun fetch(path: Path): MemoirContext? {
    val content = Files.readString(path)
    return try {
      crypto.decrypt(MemoirContext::class.java, content)
    } catch (e: Exception) {
      // log it
      null
    }
  }
}
