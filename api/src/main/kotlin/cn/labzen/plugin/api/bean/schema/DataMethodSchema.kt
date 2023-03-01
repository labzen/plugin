package cn.labzen.plugin.api.bean.schema

import java.lang.reflect.Method

open class DataMethodSchema(
  val method: Method,
  name: String,
  description: String = ""
) : DataSchema(name, method.returnType, description, true)
