package cn.labzen.plugin.api.bean.schema

import cn.labzen.plugin.api.event.Subscribable

data class SubscribeSchema(
  val subscribableClass: Class<out Subscribable>,
  val name: String,
  val version: String,
  val events: Map<String, DataMethodSchema>
)
