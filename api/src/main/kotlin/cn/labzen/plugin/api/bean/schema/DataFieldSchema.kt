package cn.labzen.plugin.api.bean.schema

import java.lang.reflect.Field

class DataFieldSchema(
  name: String,
  description: String,
  required: Boolean,
  val field: Field
) : DataSchema(name, field.type, description, required)
