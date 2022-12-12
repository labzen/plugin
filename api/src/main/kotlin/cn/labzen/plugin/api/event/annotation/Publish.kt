package cn.labzen.plugin.api.event.annotation

/**
 * 标识一个插件事件发布接口，用于主动发布事件内容
 *
 * ```java
 * @Publish(name = "user_changed", description = "用户信息被修改")
 * public interface UserInfoChangedPublisher extends Publishable {
 *
 *   @PublishEvent(description = "修改后的用户Email")
 *   void emailChanged(String email);
 * }
 * ```
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Publish(
  val name: String,
  val version: String = "1",
  val description: String
)
