package cn.labzen.plugin.api.broker

import cn.labzen.plugin.api.bean.schema.DataSchema

/**
 * 插件配置器
 */
interface Configurator {

  /**
   * 获取插件配置纲要
   */
  fun schema(): List<DataSchema>

  /**
   * 设置插件配置内容
   */
  fun configure(name: String, value: Any)
}
