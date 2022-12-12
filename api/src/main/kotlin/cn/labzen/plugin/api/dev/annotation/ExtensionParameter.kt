package cn.labzen.plugin.api.dev.annotation

/**
 * 声明功能扩展服务的执行参数
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExtensionParameter(
  val description: String,
  val required: Boolean = true
)
