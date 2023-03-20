package cn.labzen.plugin.broker.impl.memoir.bean

internal data class MemoirMount(
  val name: String,
  val identifier: String,
  val arguments: MutableMap<String, Any> = mutableMapOf()
)
