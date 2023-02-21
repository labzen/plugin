package cn.labzen.plugin.broker.specific

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.api.bean.Values
import cn.labzen.plugin.api.bean.schema.DataFieldSchema
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.bean.schema.MountSchema
import cn.labzen.plugin.api.broker.Extension
import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.annotation.MountArgument
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginOperationException
import org.reflections.ReflectionUtils
import java.util.function.Predicate
import cn.labzen.plugin.api.dev.annotation.Mount as MountAnnotation

class SpecificMount internal constructor(
  private val configurator: SpecificConfigurator,
  private val schema: MountSchema,
  private val extensionSchemas: Map<String, ExtensionSchema>
) : Mount {

  private val argumentValues = Values.withSchema(schema.arguments)
  private lateinit var instance: Mountable

  override fun setArgument(name: String, value: Any) {
    argumentValues[name] = value
  }

  override fun done() {
    if (this::instance.isInitialized) {
      return
    }

    argumentValues.validate()

    try {
      instance = schema.mountableClass.getDeclaredConstructor().newInstance()
    } catch (e: Exception) {
      throw PluginInstantiateException("无法实例化挂载组件 - ${schema.mountableClass}")
    }

    configurator.injectTo(instance)

    try {
      schema.arguments.forEach {
        argumentValues.ifPresent(it.name) { value ->
          it.field.isAccessible = true
          it.field.set(instance, value)
        }
      }
    } catch (e: Exception) {
      throw PluginInstantiateException("无法对挂载组件注入参数 - ${schema.mountableClass}")
    }

    instance.onMounted()
  }

  override fun extending(extensibleName: String): Extension {
    throwRuntimeUnless(this::instance.isInitialized) {
      PluginOperationException("组件未完成挂载")
    }

    val extensionSchema =
      extensionSchemas[extensibleName] ?: throw PluginOperationException("无效的扩展服务 - $extensibleName")
    return SpecificExtension(configurator, extensionSchema, instance)
  }

  companion object {

    internal fun scanMountableClasses(classes: List<Class<*>>): Map<String, MountSchema> {
      @Suppress("UNCHECKED_CAST")
      val mountableClasses = classes as List<Class<Mountable>>
      return mountableClasses.map(this::parseMountableClass).associateBy { it.name }
    }

    private fun parseMountableClass(mountableClass: Class<Mountable>): MountSchema {
      val mountAnnotation = mountableClass.getAnnotation(MountAnnotation::class.java)
      val declarations = mountAnnotation.declarations.map {
        Pair(it.name, it.description)
      }

      val arguments = ReflectionUtils.getAllFields(mountableClass, Predicate {
        it.isAnnotationPresent(MountArgument::class.java)
      }).map {
        val snakeName = Strings.snakeCase(it.name)
        val argumentAnnotation = it.getAnnotation(MountArgument::class.java)
        DataFieldSchema(it,snakeName, argumentAnnotation.description, argumentAnnotation.required)
      }

      return MountSchema(
        mountableClass,
        mountAnnotation.name,
        mountAnnotation.description,
        declarations,
        arguments
      )
    }
  }
}
