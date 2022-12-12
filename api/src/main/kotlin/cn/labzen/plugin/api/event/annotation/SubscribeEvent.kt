package cn.labzen.plugin.api.event.annotation

/**
 * 标识一个方法接收具体的插件时间内容
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
  val name: String = ""
)
