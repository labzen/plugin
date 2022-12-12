package cn.labzen.plugin.api.bean.schema

import cn.labzen.plugin.api.dev.Mountable

data class MountSchema(
  val mountableClass: Class<out Mountable>,
  val name: String,
  val description: String,
  val declarations: List<Pair<String, String>>,
  val arguments: List<DataFieldSchema>
)
