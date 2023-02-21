package cn.labzen.plugin.broker.spring

import cn.labzen.meta.spring.SpringApplicationContextInitializerOrder
import cn.labzen.plugin.broker.PluginBroker
import cn.labzen.plugin.broker.maven.Mavens
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.Ordered

class LabzenPluginBrokerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

  override fun getOrder(): Int =
    SpringApplicationContextInitializerOrder.MODULE_PLUGIN_INITIALIZER_ORDER

  override fun initialize(applicationContext: ConfigurableApplicationContext) {
    Mavens.initialize()
    PluginBroker.prepareApplicationSubscribes()
  }
}
