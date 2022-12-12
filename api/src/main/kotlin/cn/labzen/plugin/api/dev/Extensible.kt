package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.bean.Outcome

/**
 * 插件的实际功能扩展服务
 *
 * 示例：
 * ```java
 * public class ExamplePluginExtension implements Extensible {
 *
 *   private String username;
 *   private String result;
 *
 *   @ExtensionParameter(description = "用户名", require = true)
 *   public void setUsername(String username) {
 *     this.username = username;
 *   }
 *
 *   public String result() {
 *     return result;
 *   }
 *
 *   @Override
 *   public void execute() {
 *     // 插件功能扩展的执行部分
 *     this.result = "Hello World: " + this.username;
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
  fun destructing() {
    // do nothing
  }
}
