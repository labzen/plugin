package cn.labzen.plugin.api.broker.accessor

import cn.labzen.plugin.api.broker.Information
import cn.labzen.plugin.api.broker.Mount
import java.util.*

/**
 * 插件访问器，继承本抽象类，聚焦一个插件的所有扩展操作
 */
abstract class PluginAccessor {

  private lateinit var delegator: PluginAccessDelegator

  /**
   * 获取插件
   */
  fun plugin(): LimitedAccessPlugin = delegator.plugin()

  /**
   * 获取插件信息
   */
  fun information(): Information = delegator.information()

  /**
   * 获取插件的配置
   */
  fun configuration(): Map<String, Any?> = delegator.configuration()

  /**
   * 获取插件已经挂载的挂载组件
   */
  fun mounted(): List<Mount> = delegator.mounted()

  /**
   * 获取插件已经挂载的指定[identifier]的挂载组件
   */
  fun mounted(identifier: String): Optional<Mount> = delegator.mounted(identifier)

  /**
   * 插件已经加载；本方法被调用时，插件访问器即可使用[plugin]方法获取到插件实例进行操作
   */
  open fun loaded() {
    // ignore
  }

  /**
   * 插件已经激活（启动）
   */
  open fun activated() {
    // ignore
  }

  /**
   * 插件恢复记忆录状态，在业务上层应用（宿主）启动时，复原上次停止时插件的运行数据时调用
   */
  open fun recalled() {
    // ignore
  }

  /**
   * 插件已经失活（停止）
   */
  open fun inactivated() {
    // ignore
  }
}
