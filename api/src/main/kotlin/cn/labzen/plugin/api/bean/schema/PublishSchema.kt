package cn.labzen.plugin.api.bean.schema

import cn.labzen.plugin.api.event.Publishable

data class PublishSchema(
  val publishableClass: Class<out Publishable>,
  val name: String,
  val version: String,
  val description: String,
  val events: Map<String, DataMethodSchema>
)
