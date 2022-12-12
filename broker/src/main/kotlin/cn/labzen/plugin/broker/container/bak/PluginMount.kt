package cn.labzen.plugin.broker.container.bak// package cn.labzen.plugin.broker.container
//
// import cn.labzen.cells.core.utils.Strings
// import cn.labzen.plugin.api.bean.Values
// import cn.labzen.plugin.api.bean.schema.DataFieldSchema
// import cn.labzen.plugin.api.bean.schema.MountSchema
// import cn.labzen.plugin.api.broker.Extension
// import cn.labzen.plugin.api.broker.Mount
// import cn.labzen.plugin.api.dev.Mountable
// import cn.labzen.plugin.api.dev.Pluggable
// import cn.labzen.plugin.api.dev.annotation.MountArgument
// import cn.labzen.plugin.broker.exception.PluginInstantiateException
// import org.reflections.ReflectionUtils
// import org.reflections.Reflections
// import org.reflections.scanners.Scanners
// import org.reflections.util.ConfigurationBuilder
// import java.util.function.Predicate
// import cn.labzen.plugin.api.dev.annotation.Mount as MountAnnotation
//
// class PluginMount(private val schema: MountSchema) : Mount {
//
//   private val argumentSchemas = schema.arguments.associateBy { it.name }
//   private val argumentValues = Values(argumentSchemas)
//   private lateinit var instance: Mountable
//
//   override fun setArgument(name: String, value: Any) {
//     argumentValues[name] = value
//   }
//
//   override fun done() {
//     if (this::instance.isInitialized) {
//       return
//     }
//
//     argumentValues.validate()
//
//     try {
//       instance = schema.clazz.getDeclaredConstructor().newInstance()
//     } catch (e: Exception) {
//       throw PluginInstantiateException("无法实例化挂载组件 - ${schema.clazz}")
//     }
//
//     try {
//       schema.arguments.forEach {
//         argumentValues.ifPresent(it.name) { value ->
//           it.field.set(instance, value)
//         }
//       }
//     } catch (e: Exception) {
//       throw PluginInstantiateException("无法对挂载组件注入参数 - ${schema.clazz}")
//     }
//
//     instance.onMounted()
//   }
//
//   override fun extend(extensibleName: String): Extension {
//     TODO("到这了     开始扩展一个功能")
//   }
//
//   companion object {
//
//     fun scanMountableClasses(pluggableClass: Class<Pluggable>): Map<String, MountSchema> {
//       val classLoader = pluggableClass.classLoader
//       val rootPackage = pluggableClass.`package`.name
//       val configurationBuilder = ConfigurationBuilder()
//         .forPackage(rootPackage, classLoader)
//         .addScanners(Scanners.TypesAnnotated)
//       val reflections = Reflections(configurationBuilder)
//
//       val mountableClass = Mountable::class.java
//       val mountableClasses = reflections.getTypesAnnotatedWith(MountAnnotation::class.java)
//         .filter { !it.isInterface && mountableClass.isAssignableFrom(it) }
//         .map {
//           @Suppress("UNCHECKED_CAST")
//           it as Class<Mountable>
//         }
//
//       return mountableClasses.map(this::parseMountableClass).associateBy { it.name }
//     }
//
//     private fun parseMountableClass(mountableClass: Class<Mountable>): MountSchema {
//       val mountAnnotation = mountableClass.getAnnotation(MountAnnotation::class.java)
//       val declarations = mountAnnotation.declarations.map {
//         Pair(it.name, it.description)
//       }
//
//       val arguments = ReflectionUtils.getAllFields(mountableClass, Predicate {
//         it.isAnnotationPresent(MountArgument::class.java)
//       }).map {
//         val snakeName = Strings.snakeCase(it.name)
//         val argumentAnnotation = it.getAnnotation(MountArgument::class.java)
//         DataFieldSchema(snakeName, it.type, argumentAnnotation.description, argumentAnnotation.required, it)
//       }
//
//       return MountSchema(
//         mountableClass,
//         mountAnnotation.name,
//         mountAnnotation.description,
//         declarations,
//         arguments
//       )
//     }
//   }
// }
