package cn.labzen.plugin.broker.container.bak// package cn.labzen.plugin.broker.container
//
// import cn.labzen.cells.core.kotlin.throwRuntimeUnless
// import cn.labzen.plugin.api.bean.schema.ExtensionSchema
// import cn.labzen.plugin.api.bean.schema.MountSchema
// import cn.labzen.plugin.api.bean.Outcome
// import cn.labzen.plugin.api.broker.Extension
// import cn.labzen.plugin.api.broker.Mount
// import cn.labzen.plugin.api.dev.Pluggable
// import cn.labzen.plugin.api.dev.annotation.Configured
// import cn.labzen.plugin.broker.exception.PluginOperationException
// import cn.labzen.plugin.broker.loader.PluginLoader
// import cn.labzen.plugin.broker.resource.ResourceLoader
//
//
// /**
//  * 插件容器代理，负责面向具体插件提供容器服务
//  */
// open class PluginContainerDelegator internal constructor(private val resourceLoader: ResourceLoader) {
//
//   // private lateinit var loader: PluginLoader
//   // protected lateinit var configurator: PluginConfigurator
//   // protected lateinit var mountSchemas: Map<String, MountSchema>
//   // protected lateinit var extensionSchemas: Map<String, ExtensionSchema>
//
//   protected lateinit var identifierInContainer: String
//   private lateinit var pluggableClass: Class<Pluggable>
//   // private lateinit var pluggableInstance: Pluggable
//
//
//   // protected fun createMount(name: String): Mount {
//   //   val mountSchema = mountSchemas[name] ?: throw PluginOperationException("无效的挂载组件 - {}", name)
//   //   return PluginMount(mountSchema)
//   // }
//
//   protected fun createExtender(name: String): Extension {
//     return PluginExtension()
//   }
//
//   /**
//    * 实例化插件，并询问是否准备好激活
//    */
//   // protected fun instantiateAndCheck(): Outcome {
//   //   try {
//   //     configurator.check()
//   //   } catch (e: Exception) {
//   //     return Outcome.failed("插件配置信息不完整 - ${e.message}")
//   //   }
//   //
//   //   pluggableInstance = try {
//   //     PluginContainer.instantiatePlugin(identifierInContainer)
//   //   } catch (e: Exception) {
//   //     return Outcome.throwing("无法实例化插件主类", e)
//   //   }
//   //
//   //   // 注入配置接口实例
//   //   pluggableInstance.javaClass.fields.filter {
//   //     it.isAnnotationPresent(Configured::class.java)
//   //   }.forEach {
//   //     val configurationClass = it.type
//   //     try {
//   //       val proxy = configurator.getProxy(configurationClass)
//   //       it.set(pluggableInstance, proxy)
//   //     } catch (e: Exception) {
//   //       // todo 打日志  配置无法注入
//   //     }
//   //   }
//   //
//   //   val canActiveOutcome = pluggableInstance.canActive()
//   //   PluginContainer.canActiveAffirmed(identifierInContainer, canActiveOutcome)
//   //   return canActiveOutcome
//   // }
//
//   // protected fun preparePlugin(): Outcome {
//   //   val lifecyclePhase = PluginContainer.lifecyclePhase(identifierInContainer)
//   //   throwRuntimeUnless(lifecyclePhase == PluginLifecyclePhase.ACTIVE_PREPARED) {
//   //     PluginOperationException("插件未处于就绪状态")
//   //   }
//   //
//   //   val prepareActiveOutcome = pluggableInstance.prepareActive()
//   //   PluginContainer.activatedAffirmed(identifierInContainer, prepareActiveOutcome)
//   //   return prepareActiveOutcome
//   // }
//
//   protected fun checkAndNoticeStopConditions(): Outcome {
//     return Outcome.success()
//   }
//
//   protected fun destroyPlugin(): Outcome {
//     return Outcome.success()
//   }
//
// }
