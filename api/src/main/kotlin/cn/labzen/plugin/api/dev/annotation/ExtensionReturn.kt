package cn.labzen.plugin.api.dev.annotation

/**
 * 声明功能扩展服务的执行后结果获取方法
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExtensionReturn(
  val description: String,
  val required: Boolean = true
)
