package cn.labzen.plugin.broker.accessor

import cn.labzen.meta.Labzens
import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.api.broker.accessor.AccessPlugin
import cn.labzen.plugin.api.broker.accessor.PluginAccessor
import cn.labzen.plugin.broker.meta.PluginBrokerConfiguration
import cn.labzen.spring.helper.Springs
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

internal object PluginAccessors {

  private val accessors = mutableMapOf<Pair<String, String>, PluginAccessor>()
  private val accessorPluginField = PluginAccessor::class.java.let {
    it.getDeclaredField("plugin").also { field -> field.isAccessible = true }
  }

  /**
   * 扫描在上层应用（宿主）中的插件访问器
   */
  fun prepareApplicationPluginAccessors() {
    val configuration = Labzens.configurationWith(PluginBrokerConfiguration::class.java)

    val configurationBuilder = ConfigurationBuilder()
      .forPackages(configuration.applicationPackage())
      .addScanners(Scanners.TypesAnnotated)
    val reflections = Reflections(configurationBuilder)
    val accessorClass = PluginAccessor::class.java
    val accessorClasses = reflections.getTypesAnnotatedWith(AccessPlugin::class.java)
      .filter { !it.isInterface && accessorClass.isAssignableFrom(it) }
      .map {
        @Suppress("UNCHECKED_CAST")
        it as Class<PluginAccessor>
      }

    accessorClasses.forEach(this::parseAccessorClass)
  }

  private fun parseAccessorClass(accessorClass: Class<PluginAccessor>) {
    val accessorAnnotation = accessorClass.getAnnotation(AccessPlugin::class.java)
    val accessorInstance = Springs.getOrCreate(accessorClass)
    val key = Pair(accessorAnnotation.name, accessorAnnotation.version)

    accessors[key] = accessorInstance
  }

  fun informLoaded(name: String, version: String, plugin: Plugin) {
    val key = Pair(name, version)
    val accessor = accessors[key] ?: return

    accessorPluginField.set(accessor, plugin)

    accessor.loaded()
  }

  fun informActivated(name: String, version: String) {
    val key = Pair(name, version)
    accessors[key]?.activated()
  }

  fun informInactivated(name: String, version: String) {
    val key = Pair(name, version)
    accessors[key]?.inactivated()
  }

  fun informRecalled(name: String, version: String) {
    val key = Pair(name, version)
    accessors[key]?.recalled()
  }
}
