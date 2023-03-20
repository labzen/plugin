package cn.labzen.plugin.api.broker.accessor

import cn.labzen.plugin.api.broker.Extension
import cn.labzen.plugin.api.broker.Mount

/**
 * 有限控制的插件，用于在插件访问器中使用，防止触发重新配置、激活等操作
 */
interface LimitedAccessPlugin {

  /**
   * 获得一个新的服务扩展实例
   */
  fun extending(extensibleName: String): Extension

  /**
   * 获得一个服务扩展单例
   */
  fun extendingSingleton(extensibleName: String): Extension

  /**
   * 获得一个挂载组件
   */
  fun mounting(mountableName: String): Mount

  /**
   * 获得一个命名为[identifier]的挂载组件
   */
  fun mounting(mountableName: String, identifier: String): Mount
}
