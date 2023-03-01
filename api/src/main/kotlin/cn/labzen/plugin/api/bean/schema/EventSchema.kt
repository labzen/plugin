package cn.labzen.plugin.api.bean.schema

import cn.labzen.plugin.api.dev.Mountable
import java.lang.reflect.Method

class EventSchema(
  method: Method,
  name: String,
  val mountableClass: Class<out Mountable>?,
  description: String = ""
) : DataMethodSchema(method, name, description)
