package cn.labzen.plugin.broker.loader

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.broker.exception.PluginResourceInvalidException
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import cn.labzen.plugin.broker.loader.jcl.PluginClassLoader
import cn.labzen.plugin.broker.maven.Mavens.MAVEN_JAR_FILE_EXTENSION
import cn.labzen.plugin.broker.resource.ResourceLoader
import cn.labzen.plugin.broker.xml.PluginInformation
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.boot.loader.jar.JarFile
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.util.jar.JarEntry

internal class PluginLoader(private val resourceLoader: ResourceLoader) {

  private lateinit var information: PluginInformation

  private lateinit var dependenciesClassLoader: ClassLoader
  private lateinit var pluginClassLoader: ClassLoader

  /**
   * 加载插件基础信息
   */
  internal fun loadInformation(): PluginInformation {
    val bomContent = readPluginBomFile()
    val mapper = XmlMapper()
    return try {
      mapper.readValue(bomContent, PluginInformation::class.java)
    } catch (e: Exception) {
      throw PluginResourceInvalidException(e, "插件的配置文件labzen-plugin.xml内容无法解析")
    }.also { information = it }
  }

  /**
   * 创建插件相关的 class loader
   */
  internal fun createClassLoaders() {
    val apiVersion = information.based
    throwRuntimeUnless(apiVersion == API_BASED_VERSION) {
      PluginResourceLoadException("当前插件代理只支持加载基于 version {} 的API开发的插件")
    }

    dependenciesClassLoader = createDependenciesClassLoader()
    pluginClassLoader = createPluginClassLoader()
  }

  /**
   * 加载插件主类
   */
  internal fun loadPluggableClass(): Class<Pluggable> {
    val pluggableClassName = information.pluggable
    return try {
      @Suppress("UNCHECKED_CAST")
      pluginClassLoader.loadClass(pluggableClassName) as Class<Pluggable>
    } catch (e: Exception) {
      throw PluginResourceLoadException(e, "无法加载插件主类：{}", pluggableClassName)
    }
  }

  private fun readPluginBomFile(): String {
    val resourceUrl = resourceLoader.getUrl()
    val plugin = try {
      File(resourceUrl.toURI())
    } catch (e: URISyntaxException) {
      throw PluginResourceLoadException(e, "插件地址错误：{}", resourceUrl)
    }

    throwRuntimeUnless(plugin.exists()) {
      PluginResourceLoadException("插件不存在：{}", resourceUrl)
    }

    return if (plugin.isDirectory) {
      readPluginBomFromDirectory(plugin)
    } else {
      readPluginBomFromFile(plugin)
    }
  }

  private fun readPluginBomFromDirectory(directory: File): String {
    var bomFile = File(directory, "src/main/resources/$BOM_XML_FILE")
    if (!bomFile.exists()) {
      bomFile = File(directory, "target/classes/$BOM_XML_FILE")
    }

    throwRuntimeUnless(bomFile.exists()) {
      PluginResourceLoadException("在插件目录中找不到labzen-plugin.xml")
    }

    return Files.readString(bomFile.toPath())
  }

  private fun readPluginBomFromFile(file: File): String {
    throwRuntimeUnless(file.name.endsWith(MAVEN_JAR_FILE_EXTENSION)) {
      PluginResourceLoadException("只接受插件以Jar包形式存在")
    }

    return try {
      JarFile(file).use { jar ->
        @Suppress("INACCESSIBLE_TYPE")
        val bomEntry: JarEntry? = jar.getJarEntry(BOM_XML_FILE)
        if (bomEntry == null || bomEntry.isDirectory) {
          throw PluginResourceLoadException("在插件Jar中找不到labzen-plugin.xml")
        }
        val data = ByteArray(bomEntry.size.toInt())
        jar.getInputStream(bomEntry).use { `is` ->
          `is`.read(data)
          String(data)
        }
      }
    } catch (e: IOException) {
      throw PluginResourceLoadException(e, "无法解析jar文件：{}", file)
    }
  }

  /**
   * 创建插件依赖包的 class loader
   */
  private fun createDependenciesClassLoader(): ClassLoader {
    val associates = resourceLoader.associates()
    return PluginClassLoader(
      "plugin-dependencies$${information.name}",
      associates.toTypedArray(),
      this.javaClass.classLoader
    )
  }

  /**
   * 创建插件自用的 class loader
   */
  private fun createPluginClassLoader(): ClassLoader {
    val pluginSource = resourceLoader.getUrl()
    val pluginFile = File(pluginSource.toURI())

    return if (pluginFile.isFile) {
      PluginClassLoader("plugin$${information.name}", arrayOf(pluginSource), dependenciesClassLoader)
    } else {
      val classesDirectory = File(pluginFile, "target/classes")
      throwRuntimeUnless(classesDirectory.exists()) {
        PluginResourceLoadException("插件maven target目录不存在")
      }

      val classesSource = classesDirectory.toURI().toURL()
      PluginClassLoader("plugin$${information.name}", arrayOf(classesSource), dependenciesClassLoader)
    }
  }

  companion object {
    private const val BOM_XML_FILE = "labzen-plugin.xml"

    internal const val API_BASED_VERSION = "1.0-SNAPSHOT"
  }
}
