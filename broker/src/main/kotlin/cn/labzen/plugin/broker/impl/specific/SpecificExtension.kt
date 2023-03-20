package cn.labzen.plugin.broker.impl.specific

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.Values
import cn.labzen.plugin.api.bean.schema.DataFieldSchema
import cn.labzen.plugin.api.bean.schema.DataSchema
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.broker.Extension
import cn.labzen.plugin.api.dev.Extensible
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.annotation.ExtensionProperty
import cn.labzen.plugin.api.dev.annotation.Mounted
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import com.google.common.collect.HashBiMap
import org.reflections.ReflectionUtils
import java.util.function.Predicate
import java.util.function.Supplier
import cn.labzen.plugin.api.dev.annotation.Extension as ExtensionAnnotation

class SpecificExtension internal constructor(
  configurator: SpecificConfigurator,
  private val schema: ExtensionSchema,
  private val mountableInstance: Mountable? = null
) : Extension {

  private val inputParameterValues = Values.withSchema(schema.inputParameters)
  private val instance: Extensible

  init {
    try {
      instance = schema.extensibleClass.getDeclaredConstructor().newInstance()
    } catch (e: Exception) {
      throw PluginInstantiateException("无法实例化扩展服务 - ${schema.extensibleClass}")
    }

    configurator.injectTo(instance)
  }

  override fun setParameter(name: String, value: Any?) {
    inputParameterValues[name] = value
  }

  override fun destructing() {
    INSTANCE_HOLDER.inverse().remove(this)
    try {
      instance.onDestructing()
    } catch (e: Exception) {
      // log it
    }
  }

  @Synchronized
  override fun invoke(): Outcome {
    schema.mountedFiled?.apply {
      mountableInstance ?: throw PluginInstantiateException("无法为扩展服务注入挂载组件")
      throwRuntimeUnless(mountableInstance.javaClass.isAssignableFrom(this.type)) {
        PluginInstantiateException(
          "为扩展服务 [{}] 注入的挂载组件类型 [{}]，无法转换为注解的挂载组件类型 [{}]",
          schema.name,
          mountableInstance.javaClass,
          this.type
        )
      }
    }

    try {
      inputParameterValues.validate()
    } catch (e: Exception) {
      return Outcome.failed(e.message ?: "参数校验失败")
    }

    try {
      schema.mountedFiled?.let {
        it.isAccessible = true
        it.set(instance, mountableInstance)
      }
    } catch (e: Exception) {
      throw PluginInstantiateException("无法对扩展服务 [${schema.extensibleClass}] 注入挂载组件")
    }

    try {
      schema.inputParameters.forEach {
        inputParameterValues.ifPresent(it.name) { value ->
          it.field.isAccessible = true
          it.field.set(instance, value)
        }
      }
    } catch (e: Exception) {
      throw PluginInstantiateException("无法对扩展服务 [${schema.extensibleClass}] 注入参数")
    }

    return instance.execute()
  }

  companion object {

    private val INSTANCE_HOLDER = HashBiMap.create<InstanceHoldKey, Extension>()

    internal data class InstanceHoldKey(val extensionName: String, val mountSymbol: String? = null) {
      override fun toString(): String = "$extensionName-${mountSymbol ?: "<global>"}"
    }

    internal fun hold(extensionName: String, mountSymbol: String? = null, supplier: Supplier<Extension>) =
      INSTANCE_HOLDER.computeIfAbsent(InstanceHoldKey(extensionName, mountSymbol)) { supplier.get() }

    // ===============

    internal fun scanExtensibleClasses(classes: List<Class<*>>): Map<String, ExtensionSchema> {
      @Suppress("UNCHECKED_CAST")
      val extensibleClasses = classes as List<Class<Extensible>>
      return extensibleClasses.map(this::parseExtensibleClass).associateBy { it.name }
    }

    private fun parseExtensibleClass(extensibleClass: Class<Extensible>): ExtensionSchema {
      val extensionAnnotation = extensibleClass.getAnnotation(ExtensionAnnotation::class.java)

      val inputParameters = parseExtensionInputs(extensibleClass)

      val outputParameters = parseExtensionOutputs(extensionAnnotation)

      val mountedFields = ReflectionUtils.getAllFields(extensibleClass, Predicate {
        it.isAnnotationPresent(Mounted::class.java) && Mountable::class.java.isAssignableFrom(it.type)
      })
      val mountedField = if (mountedFields.size == 1) {
        mountedFields.first()
      } else if (mountedFields.size > 1)
        throw PluginResourceLoadException("扩展服务类中只能注入一个挂载组件")
      else null

      return ExtensionSchema(
        extensibleClass,
        extensionAnnotation.name,
        extensionAnnotation.description,
        inputParameters,
        outputParameters,
        mountedField
      )
    }

    private fun parseExtensionPropertyType(schema: DataSchema) {
      ReflectionUtils.getAllFields(schema.type, Predicate {
        it.isAnnotationPresent(ExtensionProperty::class.java)
      }).map {
        val propertyAnnotation = it.getAnnotation(ExtensionProperty::class.java)
        val propertyName = propertyAnnotation.name.ifBlank { it.name }
        DataSchema(propertyName, it.type, propertyAnnotation.description, propertyAnnotation.required).also { schema ->
          parseExtensionPropertyType(schema)
        }
      }.apply {
        schema.subs.addAll(this)
      }
    }

    private fun parseExtensionInputs(extensibleClass: Class<Extensible>): List<DataFieldSchema> {
      return ReflectionUtils.getAllFields(extensibleClass, Predicate {
        it.isAnnotationPresent(ExtensionProperty::class.java)
      }).map {
        val propertyAnnotation = it.getAnnotation(ExtensionProperty::class.java)
        val propertyName = propertyAnnotation.name.ifBlank { it.name }
        DataFieldSchema(it, propertyName, propertyAnnotation.description, propertyAnnotation.required)
      }
    }

    private fun parseExtensionOutputs(extensionAnnotation: ExtensionAnnotation): List<DataSchema> {
      return extensionAnnotation.results.map {
        DataSchema(it.name, it.type.java, it.description, it.required).also { schema ->
          parseExtensionPropertyType(schema)
        }
      }
    }
  }
}
