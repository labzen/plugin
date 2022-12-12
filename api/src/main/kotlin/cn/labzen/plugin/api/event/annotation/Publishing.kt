package cn.labzen.plugin.api.event.annotation

/**
 * 需要发布事件时，在实现 Pluggable 或 Extensible 的类中，使用该注解，定义相应的事件发布接口属性
 *
 * UserInfoChangedPublisher发布接口，参考 [Publish]
 * ```java
 * public class ExamplePlugin implements Pluggable {
 *
 *   @Publishing
 *   private UserInfoChangedPublisher publisher;
 *
 *   @Override
 *   public boolean canActive() {
 *     publisher.emailChanged("");
 *     return;
 *   }
 *
 *   ....
 * }
 * ```
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated(message = "考虑使用 Publisher 来统一获取")
annotation class Publishing
