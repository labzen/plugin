@file:Suppress("unused")

package cn.labzen.plugin.broker

import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.broker.loader.PluginLoader
import cn.labzen.plugin.broker.maven.Mavens
import cn.labzen.plugin.broker.memoir.Memoirs
import cn.labzen.plugin.broker.memoir.bean.MemoirContext
import cn.labzen.plugin.broker.memoir.memorable.MemorablePlugin
import cn.labzen.plugin.broker.resource.JarFileResourceLoader
import cn.labzen.plugin.broker.resource.MavenDirectoryResourceLoader
import cn.labzen.plugin.broker.resource.MavenJarFileResourceLoader
import cn.labzen.plugin.broker.resource.ResourceLoader
import cn.labzen.plugin.broker.specific.SpecificPlugin
import java.io.File

class PluginBroker private constructor(private val resourceLoader: ResourceLoader) {

  private var plugin: Plugin? = null

  /**
   * 加载插件到容器中（JVM），并未实例化
   */
  fun load(): Plugin =
    Memoirs.make(internalLoad(), null, resourceLoader)

  internal fun load(context: MemoirContext): Plugin =
    Memoirs.make(internalLoad(), context, resourceLoader)

  private fun internalLoad(): SpecificPlugin {
    val loader = PluginLoader(resourceLoader)

    val information = loader.loadInformation()

    loader.createClassLoaders()
    val pluggableClass = loader.loadPluggableClass()

    return SpecificPlugin(information, pluggableClass)
  }

  fun unload() {
    if (plugin is MemorablePlugin) {
      // todo 从磁盘上删除jar和响应文件
    }
    plugin = null
  }

  companion object {

    /**
     * 加载一个以本地Jar文件形式存在的插件
     * @param path 本地Jar文件地址
     */
    @JvmStatic
    fun fromJarFile(path: String): PluginBroker =
      fromJarFile(File(path))

    /**
     * 加载一个以本地Jar文件形式存在的插件
     * @param file 本地Jar文件地址
     */
    @JvmStatic
    fun fromJarFile(file: File): PluginBroker =
      PluginBroker(JarFileResourceLoader(file))

    internal fun fromJarFileWithMemoir(file: File, context: MemoirContext): PluginBroker =
      PluginBroker(JarFileResourceLoader(file, context))

    /**
     * 加载一个存在于Maven仓库中的Jar形式存在的插件，如果本地仓库中有，则不需要远程下载该Jar
     * @param coordinate Jar包的Maven坐标
     */
    @JvmStatic
    fun fromMavenJar(coordinate: String): PluginBroker =
      PluginBroker(MavenJarFileResourceLoader(Mavens.parseCoordinate(coordinate)))

    /**
     * 加载一个在本地以Maven构建的项目目录形式的插件，一般用于本地开发环境使用
     * @param path 本地项目目录地址
     */
    @JvmStatic
    fun fromMavenProjectDirectory(path: String): PluginBroker =
      fromMavenProjectDirectory(File(path))

    /**
     * 加载一个在本地以Maven构建的项目目录形式的插件，一般用于本地开发环境使用
     * @param directory 本地项目目录地址
     */
    @JvmStatic
    fun fromMavenProjectDirectory(directory: File): PluginBroker =
      PluginBroker(MavenDirectoryResourceLoader(directory))
  }
}
