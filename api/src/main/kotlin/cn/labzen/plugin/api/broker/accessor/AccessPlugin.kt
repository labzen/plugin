package cn.labzen.plugin.api.broker.accessor

import org.springframework.stereotype.Component

/**
 * 标识插件访问器的目标信息
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class AccessPlugin(
  val name: String,
  val version: String = "1"
)
