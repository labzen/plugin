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
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.dev.annotation.MountArgument
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginOperationException
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.function.Predicate
import cn.labzen.plugin.api.dev.annotation.Mount as MountAnnotation

class SpecificMount internal constructor(
  private val schema: MountSchema,
  private val extensionSchemas: Map<String, ExtensionSchema>
) : Mount {

  private val argumentValues = Values(schema.arguments)
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
    return SpecificExtension(extensionSchema, instance)
  }

  companion object {

    internal fun scanMountableClasses(pluggableClass: Class<Pluggable>): Map<String, MountSchema> {
      val classLoader = pluggableClass.classLoader
      val rootPackage = pluggableClass.`package`.name
      val configurationBuilder = ConfigurationBuilder()
        .forPackage(rootPackage, classLoader)
        .addScanners(Scanners.TypesAnnotated)
      val reflections = Reflections(configurationBuilder)

      val mountableClass = Mountable::class.java
      val mountableClasses = reflections.getTypesAnnotatedWith(MountAnnotation::class.java)
        .filter { !it.isInterface && mountableClass.isAssignableFrom(it) }
        .map {
          @Suppress("UNCHECKED_CAST")
          it as Class<Mountable>
        }

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
        DataFieldSchema(snakeName, argumentAnnotation.description, argumentAnnotation.required, it)
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
