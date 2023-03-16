package cn.labzen.plugin.broker.memoir.memorable

import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.broker.memoir.bean.MemoirMount
import cn.labzen.plugin.broker.specific.SpecificMount

internal class MemorableMount(
  mountName: String,
  private val specific: SpecificMount,
  private val plugin: MemorablePlugin
) : Mount by specific {

  private val mountable = MemoirMount(mountName, specific.getSymbol())

  override fun setArgument(name: String, value: Any) {
    specific.setArgument(name, value)
    mountable.arguments[name] = value
  }

  override fun done() {
    specific.done()
    plugin.addMountable(mountable)
  }

  override fun unmounting() {
    specific.unmounting()
    plugin.removeMountable(mountable)
  }
}
