package cn.labzen.plugin.broker.memoir

import cn.labzen.meta.Labzens
import cn.labzen.plugin.api.bean.Outcome.PluginOperateStatus.*
import cn.labzen.plugin.api.broker.Information
import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.broker.PluginBroker
import cn.labzen.plugin.broker.accessor.PluginAccessors
import cn.labzen.plugin.broker.exception.PluginUrnException
import cn.labzen.plugin.broker.memoir.bean.MemoirContext
import cn.labzen.plugin.broker.memoir.crypto.AesCrypto
import cn.labzen.plugin.broker.memoir.crypto.PlaintextCrypto
import cn.labzen.plugin.broker.memoir.memorable.MemorablePlugin
import cn.labzen.plugin.broker.meta.PluginBrokerConfiguration
import cn.labzen.plugin.broker.resource.ResourceLoader
import cn.labzen.plugin.broker.specific.SpecificPlugin
import java.io.File

object Memoirs {

  private const val MEMOIR_FILE_EXTENSION = "plugin"

  @Deprecated("")
  private const val UNBOUNDED_MEMOIR_FILE_EXTENSION = "unbounded"

  private var enabled = false
  private lateinit var memoirsDir: File
  private var secretEnabled = false

  internal fun initialize() {
    val configuration = Labzens.configurationWith(PluginBrokerConfiguration::class.java)
    enabled = configuration.memoirEnable()
    if (!enabled) {
      return
    }

    memoirsDir = configuration.memoirStorePath()?.let { File(it) }
      ?: throw PluginUrnException("启用了插件回忆录机制，必须提供一个存放插件相关文件的位置")

    val crypto = configuration.memoirSecretKey()?.let {
      secretEnabled = true
      AesCrypto(it)
    } ?: PlaintextCrypto()
    ContextAccessor.setCrypto(crypto)

    readAll()
  }

  /**
   * 读取已有的插件回忆录，并进行恢复
   */
  private fun readAll() {
    memoirsDir.listFiles()?.filter {
      it.isFile && it.extension == "jar"
    }?.forEach(this::readPlugin)
  }

  private fun readPlugin(pluginFile: File) {
    val memoirFileName = pluginFile.name.removeSuffix("jar") + MEMOIR_FILE_EXTENSION
    val memoirFile = File(pluginFile.parentFile, memoirFileName)
    if (!memoirFile.exists() || !memoirFile.isFile) {
      // log error
      return
    }

    // log no context
    val context = ContextAccessor.fetch(memoirFile.toPath()) ?: return
    // log context status

    val broker = PluginBroker.fromJarFileWithMemoir(pluginFile, context)
    val plugin = broker.load(context)
    val configurator = plugin.getConfigurator()
    context.configuration.forEach { (key, value) ->
      configurator.configure(key, value)
    }
    val prepareActivateOutcome = plugin.prepareActivate()
    if (prepareActivateOutcome.status == WARING ||
      prepareActivateOutcome.status == FAILED ||
      prepareActivateOutcome.status == EXCEPTION_THROWN
    ) {
      // log it activate error or retry
      return
    }

    val activatingOutcome = plugin.activating()
    if (activatingOutcome.status == WARING ||
      activatingOutcome.status == FAILED ||
      activatingOutcome.status == EXCEPTION_THROWN
    ) {
      // log it activate error or retry
      return
    }

    val rebuildMounts = context.rebuildMounts()
    rebuildMounts.forEach { mm ->
      val mount = plugin.mounting(mm.name, mm.identifier)
      mm.arguments.forEach { (key, value) ->
        mount.setArgument(key, value)
      }
      mount.done()
    }

    PluginAccessors.informRecalled(plugin.information().name(), plugin.information().version())
  }

  /**
   * 为插件创建一个回忆录
   */
  internal fun makeIfEnabled(
    plugin: SpecificPlugin,
    existedContext: MemoirContext?,
    resourceLoader: ResourceLoader
  ): Plugin {
    if (!enabled) {
      return plugin
    }

    val file = memoirFile(plugin.information())
    return MemorablePlugin(plugin, existedContext, file, resourceLoader)/*.recordablePlugin()*/
  }

  private fun memoirFile(information: Information): File {
    val fileName = with(information) {
      "${name()}-${version()}.$MEMOIR_FILE_EXTENSION"
    }
    return File(memoirsDir, fileName)
  }
}
