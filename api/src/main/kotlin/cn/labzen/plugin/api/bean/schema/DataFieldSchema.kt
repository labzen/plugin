package cn.labzen.plugin.api.bean.schema

import java.lang.reflect.Field

class DataFieldSchema(
  val field: Field,
  name: String,
  description: String,
  required: Boolean
) : DataSchema(name, field.type, description, required)
