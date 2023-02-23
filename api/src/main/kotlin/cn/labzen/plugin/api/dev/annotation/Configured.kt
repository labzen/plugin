package cn.labzen.plugin.api.dev.annotation

import cn.labzen.plugin.api.dev.Extensible
import cn.labzen.plugin.api.dev.Pluggable

/**
 * 需要读取配置接口定义的配置项时，在实现 [Pluggable] 或 [Extensible] 的类中，使用该注解，定义相应的配置接口属性
 *
 * ExamplePluginConfiguration配置接口，参考 [Configuration]
 * ```java
 * public class ExamplePlugin implements Pluggable {
 *
 *   @Configured
 *   private ExamplePluginConfiguration configuration;
 *
 *   @Override
 *   public boolean canActive() {
 *     return configuration.host() != null;
 *   }
 *
 *   ....
 * }
 * ```
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configured
