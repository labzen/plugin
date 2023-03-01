package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.bean.Outcome

/**
 * 插件的实际功能扩展服务
 *
 * 示例：
 * ```java
 * @Extension(name = "hello_user", description = "跟用户打招呼",
 *            results = {
 *              @ExtensionProperty(name="message", type = String.class, description = "信息")
 *            })
 * public class ExamplePluginExtension implements Extensible {
 *
 *   @ExtensionProperty(description = "用户名", require = true)
 *   private String username;
 *
 *   @Override
 *   public Outcome execute() {
 *     // 插件功能扩展的执行部分
 *     Values values = new Values("message", "Hi U: " + this.username);
 *     return Outcome.success(values);
 *   }
 * }
 * ```
 */
interface Extensible {

  /**
   * 扩展功能被执行时调用
   */
  fun execute(): Outcome

  /**
   * 当扩展服务实例被销毁时调用
   */
  fun onDestructing() {
    // do nothing
  }
}
