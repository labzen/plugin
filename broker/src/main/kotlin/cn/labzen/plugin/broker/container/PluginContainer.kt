// package cn.labzen.plugin.broker.container
//
// import cn.labzen.plugin.api.bean.Outcome
// import cn.labzen.plugin.api.definition.PluginOperateStatus
// import cn.labzen.plugin.api.dev.Pluggable
// import cn.labzen.plugin.broker.exception.PluginInstantiateException
// import cn.labzen.plugin.broker.specific.SpecificPlugin
// import cn.labzen.plugin.broker.xml.PluginInformation
// import java.lang.reflect.InvocationTargetException
//
// internal object PluginContainer {
//
//   private val pluginInformationMap = mutableMapOf<String, PluginInformation>()
//   private val pluginClasses = mutableMapOf<String, Class<Pluggable>>()
//   private val pluginInstances = mutableMapOf<String, Pluggable>()
//   private val pluginLifecyclePhases = mutableMapOf<String, PluginLifecyclePhase>()
//
//   fun lifecyclePhase(containerKey: String) = pluginLifecyclePhases[containerKey]
//
//   // fun takeNewPlugin(pluginInformation: PluginInformation): String {
//   //   val containerKey = "${pluginInformation.name}@${pluginInformation.version}"
//   //   pluginInformationMap[containerKey] = pluginInformation
//   //   pluginLifecyclePhases[containerKey] = PluginLifecyclePhase.RESOURCE_INFORMATION_READ
//   //   return containerKey
//   // }
//
//   // fun information(containerKey: String) = pluginInformationMap[containerKey]
//   //
//   // fun loadedPlugin(containerKey: String, pluggableClass: Class<Pluggable>) {
//   //   pluginClasses[containerKey] = pluggableClass
//   //   pluginLifecyclePhases[containerKey] = PluginLifecyclePhase.LOADED_INTO_JVM
//   // }
//   //
//   // fun loadedPlugin(specificPlugin: SpecificPlugin) {
//   // }
//   //
//   // private fun pluggableClass(containerKey: String) = pluginClasses[containerKey]
//
//   // fun instantiatePlugin(containerKey: String): Pluggable {
//   //   val instance = try {
//   //     pluginClasses[containerKey]!!.getConstructor().newInstance()
//   //   } catch (e: NoSuchMethodException) {
//   //     throw PluginInstantiateException(e, "插件主类需要一个无参构造函数")
//   //   } catch (e: IllegalAccessException) {
//   //     throw PluginInstantiateException(e, "插件主类需要一个公共的构造函数")
//   //   } catch (e: InstantiationException) {
//   //     throw PluginInstantiateException(e, "无法构建插件主类")
//   //   } catch (e: InvocationTargetException) {
//   //     throw PluginInstantiateException(e, "无法构建插件主类：${e.targetException.message}")
//   //   }
//   //   pluginInstances[containerKey] = instance
//   //   pluginLifecyclePhases[containerKey] = PluginLifecyclePhase.PLUGIN_INSTANTIATED
//   //   return instance
//   // }
//   //
//   // fun canActiveAffirmed(containerKey: String, outcome: Outcome) {
//   //   if (outcome.status == PluginOperateStatus.SUCCESS) {
//   //     pluginLifecyclePhases[containerKey] = PluginLifecyclePhase.ACTIVE_PREPARED
//   //   }
//   // }
//
//   // fun activatedAffirmed(containerKey: String, outcome: Outcome) {
//   //   if (outcome.status == PluginOperateStatus.SUCCESS) {
//   //     pluginLifecyclePhases[containerKey] = PluginLifecyclePhase.ACTIVATED
//   //   }
//   // }
// }
