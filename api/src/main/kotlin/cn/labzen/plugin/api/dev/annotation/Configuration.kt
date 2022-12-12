package cn.labzen.plugin.api.dev.annotation

/**
 * 标识插件的可配置接口，加入需要获取的配置。
 *
 * ```java
 * @Configuration(namespace = "net")
 * public interface ExamplePluginConfiguration extends Configurable {
 *
 *   // 配置key为 'net.host'，需要返回字符串的值
 *   @ConfigurationItem(description = "三方服务主机地址", require = false)
 *   String getHost();
 * }
 * ```
 * 如何使用上例配置接口，参考 [Configured]
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configuration(
  val namespace: String = ""
)
