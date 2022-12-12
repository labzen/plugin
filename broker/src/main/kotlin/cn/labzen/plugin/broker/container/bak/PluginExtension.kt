package cn.labzen.plugin.broker.container.bak// package cn.labzen.plugin.broker.container
//
// import cn.labzen.cells.core.utils.Strings
// import cn.labzen.plugin.api.bean.schema.DataSchema
// import cn.labzen.plugin.api.bean.schema.ExtensionSchema
// import cn.labzen.plugin.api.broker.Extension
// import cn.labzen.plugin.api.dev.Extensible
// import cn.labzen.plugin.api.dev.Pluggable
// import cn.labzen.plugin.api.dev.annotation.ExtensionParameter
// import cn.labzen.plugin.api.dev.annotation.ExtensionReturn
// import org.reflections.ReflectionUtils
// import org.reflections.Reflections
// import org.reflections.scanners.Scanners
// import org.reflections.util.ConfigurationBuilder
// import java.util.function.Predicate
// import cn.labzen.plugin.api.dev.annotation.Extension as ExtensionAnnotation
//
// class PluginExtension : Extension {
//
//   override fun setParameter(name: String, value: Any?) {
//     TODO("Not yet implemented")
//   }
//
//   override fun invoke() {
//     TODO("Not yet implemented")
//   }
//
//   override fun getResult(name: String) {
//     TODO("Not yet implemented")
//   }
//
//   companion object {
//
//     fun scanExtensibleClasses(pluggableClass: Class<Pluggable>): Map<String, ExtensionSchema> {
//       val classLoader = pluggableClass.classLoader
//       val rootPackage = pluggableClass.`package`.name
//       val configurationBuilder = ConfigurationBuilder()
//         .forPackage(rootPackage, classLoader)
//         .addScanners(Scanners.TypesAnnotated)
//       val reflections = Reflections(configurationBuilder)
//
//       val extensibleClass = Extensible::class.java
//       val extensibleClasses = reflections.getTypesAnnotatedWith(ExtensionAnnotation::class.java)
//         .filter { !it.isInterface && extensibleClass.isAssignableFrom(it) }
//         .map {
//           @Suppress("UNCHECKED_CAST")
//           it as Class<Extensible>
//         }
//
//       return extensibleClasses.map(this::parseExtensibleClass).associateBy { it.name }
//     }
//
//     private fun parseExtensibleClass(extensibleClass: Class<Extensible>): ExtensionSchema {
//       val extensionAnnotation = extensibleClass.getAnnotation(ExtensionAnnotation::class.java)
//
//       val inputParameters = ReflectionUtils.getAllFields(extensibleClass, Predicate {
//         it.isAnnotationPresent(ExtensionParameter::class.java)
//       }).map {
//         val snakeName = Strings.snakeCase(it.name)
//         val parameterAnnotation = it.getAnnotation(ExtensionParameter::class.java)
//         DataSchema(snakeName, it.type, parameterAnnotation.description, parameterAnnotation.required)
//       }
//
//       val outputParameters = ReflectionUtils.getAllFields(extensibleClass, Predicate {
//         it.isAnnotationPresent(ExtensionReturn::class.java)
//       }).map {
//         val snakeName = Strings.snakeCase(it.name)
//         val returnAnnotation = it.getAnnotation(ExtensionReturn::class.java)
//         DataSchema(snakeName, it.type, returnAnnotation.description, returnAnnotation.required)
//       }
//
//       return ExtensionSchema(
//         extensibleClass,
//         extensionAnnotation.name,
//         extensionAnnotation.description,
//         inputParameters,
//         outputParameters
//       )
//     }
//   }
// }
