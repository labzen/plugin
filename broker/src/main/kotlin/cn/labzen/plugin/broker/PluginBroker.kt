package cn.labzen.plugin.broker

import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.broker.event.EventDispatcher
import cn.labzen.plugin.broker.event.SpecificSubscribe
import cn.labzen.plugin.broker.loader.PluginLoader
import cn.labzen.plugin.broker.maven.Mavens
import cn.labzen.plugin.broker.resource.JarFileResourceLoader
import cn.labzen.plugin.broker.resource.MavenDirectoryResourceLoader
import cn.labzen.plugin.broker.resource.MavenJarFileResourceLoader
import cn.labzen.plugin.broker.resource.ResourceLoader
import cn.labzen.plugin.broker.specific.SpecificPlugin
import java.io.File

class PluginBroker private constructor(private val resourceLoader: ResourceLoader) {

  private lateinit var loader: PluginLoader
  private lateinit var plugin: SpecificPlugin

  /**
   * 加载插件到容器中（JVM），并未实例化
   */
  fun load(): Plugin {
    // loadFromResource()
    // createConfigurator()
    // readMountAndExtensionSchemas()

    loader = PluginLoader(resourceLoader)

    val information = loader.loadInformation()
    // val identifierInContainer = PluginContainer.takeNewPlugin(information)

    loader.createClassLoaders()
    val pluggableClass = loader.loadPluggableClass()

    plugin = SpecificPlugin(information, pluggableClass)
    return plugin
    // PluginContainer.loadedPlugin(plugin)
  }

  // /**
  //  * 插件信息
  //  */
  // fun information(): PluginInformation = PluginContainer.information(identifierInContainer)!!

  fun unload() {
    TODO("Not yet implemented")
  }

  // /**
  //  * 加载插件到容器中（JVM），并未实例化
  //  */
  // private fun loadFromResource() {
  //   loader = PluginLoader(resourceLoader)
  //
  //   val information = loader.loadInformation()
  //   val identifierInContainer = PluginContainer.takeNewPlugin(information)
  //
  //   loader.createClassLoaders()
  //   val pluggableClass = loader.loadPluggableClass()
  //
  //   plugin = SpecificPlugin(identifierInContainer, information, pluggableClass)
  //   PluginContainer.loadedPlugin(plugin)
  // }

  // private fun createConfigurator() {
  //   configurator = PluginConfigurator(pluggableClass).also {
  //     it.scanConfigurableInterfaces()
  //   }
  // }

  // private fun readMountAndExtensionSchemas() {
  //   mountSchemas = PluginMount.scanMountableClasses(pluggableClass)
  //   extensionSchemas = PluginExtension.scanExtensibleClasses(pluggableClass)
  // }

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

    internal fun prepareApplicationSubscribes() {
      val subscribeSchemas = SpecificSubscribe.scanApplicationSubscribable()
      subscribeSchemas.forEach {
        val specificSubscribe = SpecificSubscribe(it.value)
        EventDispatcher.registerSubscribe(specificSubscribe)
      }
    }
  }
}
