package cn.labzen.plugin.api.bean

import cn.labzen.cells.core.kotlin.throwRuntimeIf
import cn.labzen.plugin.api.bean.schema.DataSchema
import cn.labzen.plugin.api.exception.PluginDataValueInvalidException
import com.google.common.base.Supplier
import com.google.common.primitives.Primitives
import org.springframework.beans.BeanUtils
import java.util.*
import java.util.function.Consumer

open class Values {

  private var schemaMap: Map<String, DataSchema>? = null
  private val map: MutableMap<String, Any?> = mutableMapOf()

  private constructor(schemas: List<DataSchema>) {
    schemaMap = schemas.associateBy { it.name }
  }

  internal constructor()

  constructor(name: String, value: Any) {
    map[name] = value
  }

  constructor(n1: String, v1: Any, n2: String, v2: Any) {
    map[n1] = v1
    map[n2] = v2
  }

  constructor(n1: String, v1: Any, n2: String, v2: Any, n3: String, v3: Any) {
    map[n1] = v1
    map[n2] = v2
    map[n3] = v3
  }

  /**
   * 如果没有 [schemaMap]，则按照普通Map容器处理；如果有 [schemaMap]，则校验数据类型，如果没有对应的schema，则忽略该键值；如果有schema但类型不符合；则抛出异常
   */
  @Throws(PluginDataValueInvalidException::class)
  operator fun set(name: String, value: Any?) {
    value ?: return

    if (schemaMap == null) {
      map[name] = value
    } else {
      schemaMap!![name]?.let {
        val schemaType = if (it.type.isPrimitive) Primitives.wrap(it.type) else it.type
        val valueType = value.javaClass

        if (Objects.equals(schemaType, valueType) || schemaType.isAssignableFrom(valueType)) {
          map[name] = value
        } else {
          throw PluginDataValueInvalidException("数据类型不匹配")
        }
      }
    }
  }

  open fun isPresent(name: String): Boolean =
    map.containsKey(name) && map[name] != null

  open fun ifPresent(name: String, consumer: Consumer<Any>) {
    map[name]?.apply { consumer.accept(this) }
  }

  @Throws(NoSuchElementException::class, TypeCastException::class)
  open operator fun get(name: String): Any? =
    map[name]

  open fun <T : Any> whole(targetClass: Class<T>): T {
    if (Map::class.java.isAssignableFrom(targetClass)) {
      @Suppress("UNCHECKED_CAST")
      return map as T
    }

    val targetBean = targetClass.getDeclaredConstructor().newInstance()
    BeanUtils.getPropertyDescriptors(targetClass).forEach { pd ->
      ifPresent(pd.name) { value ->
        try {
          pd.writeMethod.invoke(targetBean, value)
        } catch (e: Exception) {
          // ignore
        }
      }
    }

    return targetBean
  }

  open fun whole(): Map<String, Any?> = map

  open fun orElse(name: String, other: Any? = null): Any? =
    map[name] ?: other

  open fun orElse(name: String, supplier: Supplier<Any?>): Any? =
    map[name] ?: supplier.get()

  open fun orThrow(name: String, supplier: Supplier<RuntimeException>): Any =
    map[name] ?: throw supplier.get()

  @Throws(PluginDataValueInvalidException::class)
  fun validate() {
    schemaMap ?: return

    schemaMap!!.forEach { (name, schema) ->
      throwRuntimeIf(schema.required && (map[name] == null || map[name].toString().isBlank())) {
        PluginDataValueInvalidException("必要的数据 [{}] 未提供", name)
      }
    }
  }

  companion object {

    fun withSchema(schemas: List<DataSchema>): Values =
      Values(schemas)

    @JvmStatic
    fun transmit(bean: Any): TransmittableValues =
      TransmittableValues(bean)
  }
}
