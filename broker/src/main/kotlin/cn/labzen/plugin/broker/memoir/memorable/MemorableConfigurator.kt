package cn.labzen.plugin.broker.memoir.memorable

import cn.labzen.plugin.api.broker.Configurator
import cn.labzen.plugin.broker.specific.SpecificConfigurator

internal class MemorableConfigurator(
  private val specificConfigurator: SpecificConfigurator,
  private val memorablePlugin: MemorablePlugin
) : Configurator by specificConfigurator {

  override fun configure(name: String, value: Any) {
    specificConfigurator.configure(name, value)
    memorablePlugin.configure(name, value)
  }
}
