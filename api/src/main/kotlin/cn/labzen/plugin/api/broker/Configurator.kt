package cn.labzen.plugin.api.broker

import cn.labzen.plugin.api.bean.schema.DataSchema

/**
 * 插件配置器
 */
interface Configurator {

  fun schema(): List<DataSchema>

  fun configure(name: String, value: Any)
}
