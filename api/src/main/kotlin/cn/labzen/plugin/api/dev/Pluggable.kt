package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.bean.Outcome

/**
 * 插件主类
 *
 * 必需要在 `labzen-plugin.xml` 文件中定义插件主类
 * ```xml
 * <plugin>
 *   <pluggable>{FQCN}</pluggable>
 *   ....
 * </plugin>
 * ```
 */
interface Pluggable {

  /**
   * 插件已加载入JVM（插件容器）后
   *
   * 当本插件类实例化后第一个被调用：询问插件是否可以激活使用；当返回false时，插件加载器将不会激活插件，并等待插件可激活时机
   *
   * 在本方法中，可以校验配置合法性、检查网络状态，运行环境等
   */
  fun canActive(): Outcome

  /**
   * 将插件激活，这里插件可以准备必须的资源，使插件功能就绪
   *
   * 当插件完全激活后，将通过事件机制通知插件注册器
   */
  fun prepareActive(): Outcome

  /**
   * 插件已激活的前提下，询问插件是否可以停止服务，使其置为失活；当返回false时，插件加载器将不会停止插件，并等待插件可失活时机
   */
  fun canInactive(): Outcome

  /**
   * 准备将插件失活，这里插件可以释放各种资源
   *
   * 当插件完全失活后，将通过事件机制通知插件注册器
   */
  fun prepareInactive(): Outcome

}
