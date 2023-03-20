package cn.labzen.plugin.broker.impl.memoir.memorable

import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.broker.impl.memoir.bean.MemoirMount
import cn.labzen.plugin.broker.impl.specific.SpecificMount

internal class MemorableMount(
  mountName: String,
  private val specificMount: SpecificMount,
  private val memorablePlugin: MemorablePlugin
) : Mount by specificMount {

  private val mountable = MemoirMount(mountName, specificMount.identifier())

  override fun setArgument(name: String, value: Any) {
    specificMount.setArgument(name, value)
    mountable.arguments[name] = value
  }

  override fun done() {
    specificMount.done()
    memorablePlugin.addMountable(mountable)
  }

  override fun unmounting() {
    specificMount.unmounting()
    memorablePlugin.removeMountable(mountable)
  }
}
