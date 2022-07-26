package cn.labzen.plugin.api.dev

/**
 * 插件主类
 *
 * 需要在 `labzen.plugin` 文件中定义插件主类实现
 * ```
 * cn.labzen.plugin=<FQC>
 * ```
 */
interface Pluggable {

  /**
   * 当插件被加载时，第一时间被触发
   */
  fun loading()

  /**
   * 当插件被加载完成后触发
   */
  fun loaded()

  /**
   * 当插件被卸载时，第一时间被触发
   */
  fun unloading()

  /**
   * 当插件被卸载完成后触发
   */
  fun unloaded()

  /**
   * 插件已加载的前提下，询问插件是否准备好，可以激活使用；当返回false时，插件加载器将不会激活插件，并等待插件可激活时机
   */
  fun canActive(): Boolean

  /**
   * 准备将插件激活，这里插件可以准备必须资源，使插件功能就绪
   *
   * 当插件完全激活后，将通过事件机制通知插件注册器
   */
  fun prepareActive()

  /**
   * 插件已激活的前提下，询问插件是否可以停止服务，使其置为失活；当返回false时，插件加载器将不会停止插件，并等待插件可失活时机
   */
  fun canInactive(): Boolean

  /**
   * 准备将插件失活，这里插件可以释放各种资源
   *
   * 当插件完全失活后，将通过事件机制通知插件注册器
   */
  fun prepareInactive()

  /**
   * 重置插件状态
   */
  fun reset() {
    // do nothing
  }

}
