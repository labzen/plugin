package cn.labzen.plugin.broker.memoir.memorable

import cn.labzen.cells.core.kotlin.equalsAny
import cn.labzen.cells.core.utils.DateTimes
import cn.labzen.plugin.api.bean.Outcome
import cn.labzen.plugin.api.bean.Outcome.PluginOperateStatus.*
import cn.labzen.plugin.api.broker.Configurator
import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.broker.memoir.ContextAccessor
import cn.labzen.plugin.broker.memoir.bean.MemoirContext
import cn.labzen.plugin.broker.memoir.bean.MemoirMount
import cn.labzen.plugin.broker.memoir.bean.PluginContextStatus
import cn.labzen.plugin.broker.memoir.bean.PluginContextStatus.*
import cn.labzen.plugin.broker.resource.ResourceLoader
import cn.labzen.plugin.broker.specific.SpecificConfigurator
import cn.labzen.plugin.broker.specific.SpecificMount
import cn.labzen.plugin.broker.specific.SpecificPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class MemorablePlugin(
  private val specific: SpecificPlugin,
  memoirContext: MemoirContext? = null,
  memoirFile: File,
  resourceLoader: ResourceLoader
) : Plugin by specific {

  private val memoirPath = memoirFile.toPath()

  private var context = memoirContext ?: MemoirContext(with(specific.information()) { "${name()}-${version()}" })

  init {
    // memoirContext 为空，代表是动态加载创建的Memorable插件，这时，需要将插件以及依赖的jar包等资源拷贝到指定的目录下，在下次插件机制跟随系统启动时恢复状态
    if (memoirContext == null) {
      // 保存原始插件jar包
      val memoirsDir = memoirFile.parentFile

      val protocol = resourceLoader.getUrl().protocol
      if (protocol.equals("file", true)) {
        copyLocalPluginJarFile(memoirsDir, resourceLoader)
      } else if (protocol.equalsAny("http", "https", ignoreCase = true)) {
        copyNetworkPluginJarFile(memoirsDir, resourceLoader)
      }
    }
  }

  private fun pluginArchiveJarFile(memoirsDir: File) =
    with(specific.information()) {
      File(memoirsDir, "${name()}-${version()}.jar")
    }

  private fun copyLocalPluginJarFile(memoirsDir: File, resourceLoader: ResourceLoader) {
    val pluginArchiveFile = pluginArchiveJarFile(memoirsDir)

    val pluginOriginalResourceFile = File(resourceLoader.getUrl().toURI())
    if (!pluginOriginalResourceFile.isFile) {
      // log 不是文件
    } else if (!pluginOriginalResourceFile.extension.equals("jar", true)) {
      // log 不是jar包
    } else {
      Files.copy(pluginOriginalResourceFile.toPath(), pluginArchiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

      val dependenciesDir = File(pluginArchiveFile.parentFile, context.dependencies)
      dependenciesDir.mkdirs()
      resourceLoader.associates().forEach {
        val originalDependencyFile = File(it.toURI())
        val archiveDependencyFile = File(dependenciesDir, originalDependencyFile.name)
        Files.copy(originalDependencyFile.toPath(), archiveDependencyFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  private fun copyNetworkPluginJarFile(memoirsDir: File, resourceLoader: ResourceLoader) {
    val pluginArchiveFile = pluginArchiveJarFile(memoirsDir)
    Files.copy(resourceLoader.getUrl().openStream(), pluginArchiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

    val dependenciesDir = File(pluginArchiveFile.parentFile, context.dependencies)
    resourceLoader.associates().forEach {
      val fileName = it.path.substringAfterLast("/")
      val archiveDependencyFile = File(dependenciesDir, fileName)
      Files.copy(it.openStream(), archiveDependencyFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
  }

  private fun pluginStatusChanged(outcome: Outcome, changeToStatus: PluginContextStatus) {
    if (outcome.status == WARING || outcome.status == FAILED || outcome.status == EXCEPTION_THROWN) {
      context.latestErrorMessage = outcome.message
    }

    if (outcome.status == SUCCESS || outcome.status == WARING) {
      context.status = changeToStatus

      if (changeToStatus == ACTIVATED) {
        context.latestActivateTime = DateTimes.formatNow("yyyy-MM-dd HH:mm:ss")
      }
      if (changeToStatus == INACTIVATED) {
        context.latestInactivateTime = DateTimes.formatNow("yyyy-MM-dd HH:mm:ss")
      }
    }

    ContextAccessor.record(memoirPath, context)
  }

  internal fun configure(name: String, value: Any) {
    context.configuration[name] = value
  }

  internal fun addMountable(mountable: MemoirMount) {
    context.mounted.add(mountable)
    ContextAccessor.record(memoirPath, context)
  }

  internal fun removeMountable(mountable: MemoirMount) {
    context.mounted.removeIf {
      mountable.identifier == it.identifier
    }
    ContextAccessor.record(memoirPath, context)
  }

  override fun getConfigurator(): Configurator {
    val configurator = specific.getConfigurator() as SpecificConfigurator
    return MemorableConfigurator(configurator, this)
  }

  override fun prepareActivate(): Outcome {
    val outcome = specific.prepareActivate()
    pluginStatusChanged(outcome, ACTIVATE_PREPARED)
    return outcome
  }

  override fun activating(): Outcome {
    val outcome = specific.activating()
    pluginStatusChanged(outcome, ACTIVATED)
    return outcome
  }

  override fun prepareInactivate(): Outcome {
    val outcome = specific.prepareInactivate()
    pluginStatusChanged(outcome, INACTIVATE_PREPARED)
    return outcome
  }

  override fun inactivating(): Outcome {
    val outcome = specific.inactivating()
    pluginStatusChanged(outcome, INACTIVATED)
    return outcome
  }

  override fun mounting(mountableName: String): Mount {
    val mount = specific.mounting(mountableName) as SpecificMount
    return MemorableMount(mountableName, mount, this)
  }

}
