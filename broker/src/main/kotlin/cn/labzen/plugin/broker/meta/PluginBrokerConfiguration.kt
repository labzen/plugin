package cn.labzen.plugin.broker.meta

import cn.labzen.meta.configuration.annotation.Configured
import cn.labzen.meta.configuration.annotation.Item
import org.slf4j.event.Level

@Configured("plugin.broker")
interface PluginBrokerConfiguration {

  @Item(path = "root", logLevel = Level.INFO)
  fun applicationPackages(): List<String>
}
