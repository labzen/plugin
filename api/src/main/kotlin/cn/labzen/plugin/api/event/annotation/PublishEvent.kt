package cn.labzen.plugin.api.event.annotation

/**
 * 声明插件主动发布的内容信息详情
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PublishEvent(
  val name: String = "",
  val description: String
)
