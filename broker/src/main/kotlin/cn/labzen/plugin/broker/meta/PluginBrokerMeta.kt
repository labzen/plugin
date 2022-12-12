package cn.labzen.plugin.broker.meta

import cn.labzen.meta.component.LabzenComponent

class PluginBrokerMeta : LabzenComponent {

  override fun description(): String =
    "插件代理模块，负责插件的加载、启动和治理"

  override fun mark(): String =
    "Labzen-Plugin-Broker"

  override fun packageBased(): String =
    "cn.labzen.plugin.broker"
}
