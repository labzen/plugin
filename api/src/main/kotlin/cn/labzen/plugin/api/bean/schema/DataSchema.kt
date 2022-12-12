package cn.labzen.plugin.api.bean.schema

open class DataSchema(
  val name: String,
  val type: Class<*>,
  val description: String,
  val required: Boolean
)
