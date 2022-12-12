package cn.labzen.plugin.broker.specific

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.Values
import cn.labzen.plugin.api.bean.schema.DataFieldSchema
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.broker.Extension
import cn.labzen.plugin.api.dev.Extensible
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.dev.annotation.ExtensionParameter
import cn.labzen.plugin.api.dev.annotation.ExtensionReturn
import cn.labzen.plugin.api.dev.annotation.Mounted
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.function.Predicate
import cn.labzen.plugin.api.dev.annotation.Extension as ExtensionAnnotation

class SpecificExtension internal constructor(
  private val schema: ExtensionSchema,
  private val mountableInstance: Mountable? = null
) : Extension {

  private val inputParameterValues = Values(schema.inputParameters)
  private lateinit var instance: Extensible

  override fun setParameter(name: String, value: Any?) {
    inputParameterValues[name] = value
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

    inputParameterValues.validate()

    if (!this::instance.isInitialized) {
      try {
        instance = schema.extensibleClass.getDeclaredConstructor().newInstance()
      } catch (e: Exception) {
        throw PluginInstantiateException("无法实例化扩展服务 - ${schema.extensibleClass}")
      }
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

  override fun <T> getResult(name: String): T? {
    val fieldSchema = schema.outputParameters.find { it.name == name } ?: throw PluginInstantiateException("")

    @Suppress("UNCHECKED_CAST")
    return fieldSchema.field.get(instance) as T?
  }

  companion object {

    internal fun scanExtensibleClasses(pluggableClass: Class<Pluggable>): Map<String, ExtensionSchema> {
      val classLoader = pluggableClass.classLoader
      val rootPackage = pluggableClass.`package`.name
      val configurationBuilder = ConfigurationBuilder()
        .forPackage(rootPackage, classLoader)
        .addScanners(Scanners.TypesAnnotated)
      val reflections = Reflections(configurationBuilder)

      val extensibleClass = Extensible::class.java
      val extensibleClasses =
        reflections.getTypesAnnotatedWith(ExtensionAnnotation::class.java)
          .filter { !it.isInterface && extensibleClass.isAssignableFrom(it) }
          .map {
            @Suppress("UNCHECKED_CAST")
            it as Class<Extensible>
          }

      return extensibleClasses.map(this::parseExtensibleClass).associateBy { it.name }
    }

    private fun parseExtensibleClass(extensibleClass: Class<Extensible>): ExtensionSchema {
      val extensionAnnotation = extensibleClass.getAnnotation(ExtensionAnnotation::class.java)

      val inputParameters = ReflectionUtils.getAllFields(extensibleClass, Predicate {
        it.isAnnotationPresent(ExtensionParameter::class.java)
      }).map {
        val snakeName = Strings.snakeCase(it.name)
        val parameterAnnotation = it.getAnnotation(ExtensionParameter::class.java)
        DataFieldSchema(snakeName, parameterAnnotation.description, parameterAnnotation.required, it)
      }

      val outputParameters = ReflectionUtils.getAllFields(extensibleClass, Predicate {
        it.isAnnotationPresent(ExtensionReturn::class.java)
      }).map {
        val snakeName = Strings.snakeCase(it.name)
        val returnAnnotation = it.getAnnotation(ExtensionReturn::class.java)
        DataFieldSchema(snakeName, returnAnnotation.description, returnAnnotation.required, it)
      }

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
  }
}
