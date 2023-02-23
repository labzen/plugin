package cn.labzen.plugin.api.dev.annotation

/**
 * 标识插件的可配置接口，加入需要获取的配置。
 *
 * 如何使用上例配置接口，参考 [Configured]
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configuration(
  val namespace: String = ""
)
