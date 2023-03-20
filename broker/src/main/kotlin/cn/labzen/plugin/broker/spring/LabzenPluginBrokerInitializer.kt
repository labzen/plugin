package cn.labzen.plugin.broker.spring

import cn.labzen.meta.spring.SpringApplicationContextInitializerOrder
import cn.labzen.plugin.broker.accessor.PluginAccessors
import cn.labzen.plugin.broker.event.PublisherDelegator
import cn.labzen.plugin.broker.maven.Mavens
import cn.labzen.plugin.broker.impl.memoir.Memoirs
import cn.labzen.plugin.broker.impl.specific.SpecificSubscribe
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.Ordered

class LabzenPluginBrokerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

  override fun getOrder(): Int =
    SpringApplicationContextInitializerOrder.MODULE_PLUGIN_INITIALIZER_ORDER

  override fun initialize(applicationContext: ConfigurableApplicationContext) {
    // 初始化 Publisher 代理
    PublisherDelegator.initialize()

    // 扫描上层应用（宿主）中的插件订阅者
    SpecificSubscribe.prepareApplicationSubscribes()

    // 扫描上层应用（宿主）中的插件访问器
    PluginAccessors.prepareApplicationPluginAccessors()

    // 初始化 Maven 相关信息
    Mavens.initialize()

    // 初始化记忆录机制，并恢复所有插件记忆录状态
    Memoirs.initialize()
  }
}
