package cn.labzen.plugin.api.bean.schema

import cn.labzen.plugin.api.dev.Extensible
import java.lang.reflect.Field

data class ExtensionSchema(
  val extensibleClass: Class<out Extensible>,
  val name: String,
  val description: String,
  val inputParameters: List<DataFieldSchema>,
  val outputParameters: List<DataFieldSchema>,
  val mountedFiled: Field?
)
