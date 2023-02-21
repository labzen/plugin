package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.dev.annotation.Configuration
import cn.labzen.plugin.api.dev.annotation.Configured

/**
 * 插件的可配置接口，开发者需继承本接口，加入需要获取的配置。
 *
 * 配置接口示例，参考 [Configuration]
 *
 * 需要读取配置接口定义的配置项时，参考 [Configured]
 * ```java
 * public interface ExamplePluginConfiguration extends Configurable {
 *
 *   // 配置key为 'host'，需要返回字符串的值
 *   @ConfigurationItem(description = "三方服务主机地址", require = false)
 *   String getHost();
 * }
 * ```
 */
interface Configurable
