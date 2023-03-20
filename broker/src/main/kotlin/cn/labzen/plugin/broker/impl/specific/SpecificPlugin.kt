package cn.labzen.plugin.broker.impl.specific

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.cells.core.utils.Randoms
import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.Outcome.PluginOperateStatus.SUCCESS
import cn.labzen.plugin.api.bean.Outcome.PluginOperateStatus.WARING
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.bean.schema.MountSchema
import cn.labzen.plugin.api.bean.schema.PublishSchema
import cn.labzen.plugin.api.bean.schema.SubscribeSchema
import cn.labzen.plugin.api.broker.*
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.broker.accessor.PluginAccessors
import cn.labzen.plugin.broker.event.EventDispatcher
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginOperationException
import cn.labzen.plugin.broker.impl.specific.SpecificConfigurator
import cn.labzen.plugin.broker.impl.specific.SpecificExtension
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector.Companion.CONFIGURABLE_CLASSES
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector.Companion.EXTENSIBLE_CLASSES
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector.Companion.MOUNTABLE_CLASSES
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector.Companion.PUBLISHABLE_CLASSES
import cn.labzen.plugin.broker.impl.specific.reflection.PluginReflector.Companion.SUBSCRIBABLE_CLASSES
import cn.labzen.plugin.broker.xml.PluginInformation
import java.lang.reflect.InvocationTargetException
import java.util.*

class SpecificPlugin internal constructor(
  private val information: PluginInformation,
  private val pluggableClass: Class<Pluggable>
) : Plugin {

  private val configurator: SpecificConfigurator
  private val mountSchemas: Map<String, MountSchema>
  private val extensionSchemas: Map<String, ExtensionSchema>
  private val publishSchemas: Map<String, PublishSchema>
  private val subscribeSchemas: Map<String, SubscribeSchema>

  private lateinit var pluggableInstance: Pluggable

  @Deprecated("这俩状态变量，感觉作用不大")
  private var prepared = false
  private var activated = false

  init {
    val reflector = PluginReflector(pluggableClass)
    reflector.scan()

    configurator = SpecificConfigurator(reflector.get(CONFIGURABLE_CLASSES)!!)
    mountSchemas = SpecificMount.scanMountableClasses(reflector.get(MOUNTABLE_CLASSES)!!)
    extensionSchemas = SpecificExtension.scanExtensibleClasses(reflector.get(EXTENSIBLE_CLASSES)!!)
    publishSchemas = SpecificPublish.scanPublishableInterfaces(reflector.get(PUBLISHABLE_CLASSES)!!)
    subscribeSchemas = SpecificSubscribe.scanPluginSubscribable(reflector.get(SUBSCRIBABLE_CLASSES)!!)
  }

  override fun information(): Information = information

  override fun getConfigurator(): Configurator = configurator

  override fun prepareActivate(): Outcome {
    try {
      configurator.check()
    } catch (e: Exception) {
      return Outcome.failed("插件配置信息不完整 - ${e.message}")
    }

    pluggableInstance = try {
      instantiatePlugin()
    } catch (e: Exception) {
      return Outcome.throwing("无法实例化插件主类", e)
    }

    publishSchemas.forEach {
      val specificPublish = SpecificPublish(it.value)
      EventDispatcher.registerPublish(specificPublish)
    }
    subscribeSchemas.forEach {
      val specificSubscribe = SpecificSubscribe(it.value)
      EventDispatcher.registerSubscribe(specificSubscribe)
    }

    configurator.injectTo(pluggableInstance)

    val canActiveOutcome = pluggableInstance.canActive()
    prepared = canActiveOutcome.status == SUCCESS || canActiveOutcome.status == WARING
    return canActiveOutcome
  }

  private fun instantiatePlugin(): Pluggable {
    val instance = try {
      pluggableClass.getConstructor().newInstance()
    } catch (e: NoSuchMethodException) {
      throw PluginInstantiateException(e, "插件主类需要一个无参构造函数")
    } catch (e: IllegalAccessException) {
      throw PluginInstantiateException(e, "插件主类需要一个公共的构造函数")
    } catch (e: InstantiationException) {
      throw PluginInstantiateException(e, "无法构建插件主类")
    } catch (e: InvocationTargetException) {
      throw PluginInstantiateException(e, "无法构建插件主类：${e.targetException.message}")
    }
    return instance
  }

  override fun activating(): Outcome {
    throwRuntimeUnless(prepared) {
      PluginOperationException("插件未处于就绪状态")
    }

    val prepareActiveOutcome = pluggableInstance.prepareActive()
    activated = prepareActiveOutcome.status == SUCCESS || prepareActiveOutcome.status == WARING
    if (activated) {
      PluginAccessors.informActivated(information.name, information.version)
    }
    return prepareActiveOutcome
  }

  override fun prepareInactivate(): Outcome {
    return Outcome.success()
  }

  override fun inactivating(): Outcome {
    PluginAccessors.informInactivated(information.name, information.version)
    return Outcome.success()
  }

  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun extensions(): List<ExtensionSchema> = extensionSchemas.values.toList()

  override fun extending(extensibleName: String): Extension {
    val extensionSchema =
      extensionSchemas[extensibleName] ?: throw PluginOperationException("无效的扩展服务 - $extensibleName")

    extensionSchema.mountedFiled?.let {
      throw PluginOperationException("扩展服务必须通过挂载组件扩展 - ${extensionSchema.mountedFiled}")
    }

    return SpecificExtension(configurator, extensionSchema, null)
  }

  override fun extendingSingleton(extensibleName: String): Extension =
    SpecificExtension.hold(extensibleName) { extending(extensibleName) }

  override fun mounts(): List<MountSchema> = mountSchemas.values.toList()

  override fun mounting(mountableName: String): Mount {
    val identifier = Randoms.string(16, Randoms.NUMBERS_AND_LETTERS_LOWER_CASE)
    return mounting(mountableName, identifier)
  }

  override fun mounting(mountableName: String, identifier: String): Mount {
    val mountSchema =
      mountSchemas[mountableName] ?: throw PluginOperationException("无效的挂载组件 - $mountableName")

    val applicableExtensionSchemas = extensionSchemas.filter {
      it.value.mountedFiled?.type == mountSchema.mountableClass
    }
    return SpecificMount(identifier, configurator, mountSchema, applicableExtensionSchemas)
  }

  override fun publishers(): List<PublishSchema> = publishSchemas.values.toList()
}
