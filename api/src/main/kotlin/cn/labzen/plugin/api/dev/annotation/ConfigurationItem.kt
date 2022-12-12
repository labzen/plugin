package cn.labzen.plugin.api.dev.annotation

/**
 * 标识插件配置项的说明信息
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigurationItem(
  val description: String,
  val require: Boolean = true
)
