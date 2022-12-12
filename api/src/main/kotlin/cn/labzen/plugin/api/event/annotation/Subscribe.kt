package cn.labzen.plugin.api.event.annotation

import cn.labzen.plugin.api.event.Subscribable
import org.springframework.stereotype.Component

/**
 * 标识一个 [Subscribable] 接口的实现类，可接收来自插件的事件内容
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Subscribe(
  // @Deprecated(message = "靠name的唯一性来约束？？？") val plugin: String,
  val name: String,
  val version: String = "1"
)
