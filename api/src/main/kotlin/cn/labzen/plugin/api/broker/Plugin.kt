package cn.labzen.plugin.api.broker

import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.bean.schema.MountSchema
import cn.labzen.plugin.api.bean.schema.PublishSchema
import cn.labzen.plugin.api.broker.accessor.LimitedAccessPlugin

/**
 * 插件代理加载插件后的插件实例映射，通过该接口来控制插件的所有操作
 */
interface Plugin : LimitedAccessPlugin {

  /**
   * 插件信息
   */
  fun information(): Information

  /**
   * 获取适配该插件的配置器
   */
  fun getConfigurator(): Configurator

  /**
   * 准备启动（激活）插件
   */
  fun prepareActivate(): Outcome

  /**
   * 激活插件，使插件服务进入可用状态，在本方法中对插件所需的配置进行读取并应用
   */
  fun activating(): Outcome

  /**
   * 准备停止（失活）插件
   */
  fun prepareInactivate(): Outcome

  /**
   * 停止插件，使插件失活，服务不可用
   */
  fun inactivating(): Outcome

  /**
   * 重置插件状态
   */
  fun reset()

  /**
   * 插件所有的功能扩展服务信息
   */
  fun extensions(): List<ExtensionSchema>

  /**
   * 插件所有的可挂载组件
   */
  fun mounts(): List<MountSchema>

  /**
   * 插件的所有发布者以及其定义的事件
   */
  fun publishers(): List<PublishSchema>
}
