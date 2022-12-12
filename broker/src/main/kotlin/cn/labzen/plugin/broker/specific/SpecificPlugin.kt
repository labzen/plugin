package cn.labzen.plugin.broker.specific

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.schema.ExtensionSchema
import cn.labzen.plugin.api.bean.schema.MountSchema
import cn.labzen.plugin.api.broker.*
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.dev.annotation.Configured
import cn.labzen.plugin.broker.event.EventDispatcher
import cn.labzen.plugin.broker.event.SpecificPublish
import cn.labzen.plugin.broker.event.SpecificSubscribe
import cn.labzen.plugin.broker.exception.PluginInstantiateException
import cn.labzen.plugin.broker.exception.PluginOperationException
import cn.labzen.plugin.broker.xml.PluginInformation
import java.lang.reflect.InvocationTargetException

class SpecificPlugin internal constructor(
  private val information: PluginInformation,
  private val pluggableClass: Class<Pluggable>
) : Plugin {

  // todo 已经摒弃掉容器的概念了，还要一个唯一标识，有没有必要？
  val identifier = "${information.name}@${information.version}"
  private val configurator = SpecificConfigurator(pluggableClass).also {
    it.scanConfigurableInterfaces()
  }
  private val mountSchemas = SpecificMount.scanMountableClasses(pluggableClass)
  private val extensionSchemas = SpecificExtension.scanExtensibleClasses(pluggableClass)
  private val publishSchemas = SpecificPublish.scanPublishableInterfaces(pluggableClass)
  private val subscribeSchemas = SpecificSubscribe.scanPluginSubscribable(pluggableClass)

  private lateinit var pluggableInstance: Pluggable
  private var prepared = false
  private var activated = false

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

    // 注入配置接口实例
    pluggableInstance.javaClass.fields.filter {
      it.isAnnotationPresent(Configured::class.java)
    }.forEach {
      val configurationClass = it.type
      try {
        val proxy = configurator.getProxy(configurationClass)
        it.isAccessible = true
        it.set(pluggableInstance, proxy)
      } catch (e: Exception) {
        // todo 打日志  配置无法注入
      }
    }

    val canActiveOutcome = pluggableInstance.canActive()
    prepared = canActiveOutcome.status == Outcome.PluginOperateStatus.SUCCESS
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
    activated = prepareActiveOutcome.status == Outcome.PluginOperateStatus.SUCCESS
    return prepareActiveOutcome
  }

  override fun prepareInactivate(): Outcome {
    return Outcome.success()
  }

  override fun inactivating(): Outcome {
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

    return SpecificExtension(extensionSchema)
  }

  override fun mounts(): List<MountSchema> = mountSchemas.values.toList()

  override fun mounting(mountableName: String): Mount {
    val mountSchema =
      mountSchemas[mountableName] ?: throw PluginOperationException("无效的挂载组件 - $mountableName")

    val applicableExtensionSchemas = extensionSchemas.filter {
      it.value.mountedFiled?.type == mountSchema.mountableClass
    }
    return SpecificMount(mountSchema, applicableExtensionSchemas)
  }
}
