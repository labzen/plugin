package cn.labzen.plugin.broker.memoir.bean

import java.io.Serializable

internal data class MemoirContext(
  val dependencies: String,
  var status: PluginContextStatus = PluginContextStatus.LOADED,
  var latestActivateTime: String? = null,
  var latestInactivateTime: String? = null,
  var latestErrorMessage: String? = null,
  val configuration: MutableMap<String, Any> = mutableMapOf(),
  val mounted: MutableList<MemoirMount> = mutableListOf()
) : Serializable {

  fun rebuildMounts(): List<MemoirMount> {
    val copy = mounted.toList()
    mounted.clear()
    return copy
  }
}
