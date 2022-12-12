@file:Suppress("UNCHECKED_CAST")

package cn.labzen.plugin.api.bean

import cn.labzen.cells.core.kotlin.throwRuntimeIf
import cn.labzen.plugin.api.bean.schema.DataSchema
import cn.labzen.plugin.api.exception.PluginDataValueInvalidException
import com.google.common.base.Supplier
import com.google.common.primitives.Primitives
import java.util.*
import java.util.function.Consumer

class Values(schemas: List<DataSchema>? = null) {

  private val schemaMap = schemas?.associateBy { it.name }
  private val map: MutableMap<String, Any?> = mutableMapOf()

  // constructor() {
  //   map = mutableMapOf()
  // }

  // constructor(name: String, value: Any) {
  //   map = mutableMapOf(Pair(name, value))
  // }
  //
  // constructor(n1: String, v1: Any, n2: String, v2: Any) {
  //   map = mutableMapOf(Pair(n1, v1), Pair(n2, v2))
  // }
  //
  // constructor(n1: String, v1: Any, n2: String, v2: Any, n3: String, v3: Any) {
  //   map = mutableMapOf(Pair(n1, v1), Pair(n2, v2))
  // }
  //
  // constructor(n1: String, v1: Any, n2: String, v2: Any, n3: String, v3: Any, n4: String, v4: Any) {
  //   map = mutableMapOf(Pair(n1, v1), Pair(n2, v2))
  // }

  /**
   * 如果没有 [schemas]，则按照普通Map容器处理；如果有 [schemas]，则校验数据类型，如果没有对应的schema，则忽略该键值；如果有schema但类型不符合；则抛出异常
   */
  @Throws(PluginDataValueInvalidException::class)
  operator fun set(name: String, value: Any?) {
    value ?: return

    if (schemaMap == null) {
      map[name] = value
    } else {
      schemaMap[name]?.let {
        val schemaType = if (it.type.isPrimitive) Primitives.wrap(it.type) else it.type
        val valueType = value.javaClass

        if (Objects.equals(schemaType, valueType) || schemaType.isAssignableFrom(valueType)) {
          map[name] = value
        } else {
          throw PluginDataValueInvalidException("")
        }
      }
    }
  }

  fun isPresent(name: String): Boolean =
    map.containsKey(name) && map[name] != null

  fun ifPresent(name: String, consumer: Consumer<Any?>) {
    map[name] ?: return
    consumer.accept(map[name])
  }

  @Throws(NoSuchElementException::class, TypeCastException::class)
  operator fun get(name: String): Any {
    return map[name] ?: throw NoSuchElementException("")
  }

  fun orElse(name: String, other: Any? = null): Any? =
    map[name] ?: other

  fun orElse(name: String, supplier: Supplier<Any?>): Any? =
    map[name] ?: supplier.get()

  fun orThrow(name: String, supplier: Supplier<RuntimeException>): Any =
    map[name] ?: throw supplier.get()

  @Throws(PluginDataValueInvalidException::class)
  fun validate() {
    schemaMap ?: return

    schemaMap.forEach { (name, schema) ->
      throwRuntimeIf(schema.required && map[name] == null) {
        PluginDataValueInvalidException("必要的数据 [{}] 未提供", name)
      }
    }
  }
}
