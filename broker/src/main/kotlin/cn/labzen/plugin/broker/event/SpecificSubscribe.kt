package cn.labzen.plugin.broker.event

import cn.labzen.cells.core.utils.Strings
import cn.labzen.meta.Labzens
import cn.labzen.plugin.api.bean.schema.DataMethodSchema
import cn.labzen.plugin.api.bean.schema.SubscribeSchema
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.event.Subscribable
import cn.labzen.plugin.api.event.annotation.Subscribe
import cn.labzen.plugin.api.event.annotation.SubscribeEvent
import cn.labzen.plugin.broker.meta.PluginBrokerConfiguration
import cn.labzen.spring.helper.Springs
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.function.Predicate

class SpecificSubscribe internal constructor(internal val schema: SubscribeSchema) {

  internal val instance = Springs.getOrCreate(schema.subscribableClass)

  companion object {

    /**
     * 扫描在上层应用中的订阅
     */
    internal fun scanApplicationSubscribable(): Map<String, SubscribeSchema> {
      val configuration = Labzens.configurationWith(PluginBrokerConfiguration::class.java)

      val configurationBuilder = ConfigurationBuilder()
        .forPackages(*configuration.applicationPackages().toTypedArray())
        .addScanners(Scanners.TypesAnnotated)

      val subscribableClasses = reflection(configurationBuilder)

      return subscribableClasses.map(this::parseSubscribableClass).associateBy { it.name }
      // todo 扫描每个类中订阅的具体事件方法，跟发布specific一样，把两边的对应关系放到一个单独的代理类中处理
    }

    /**
     * 扫描在当前加载插件中的订阅
     */
    internal fun scanPluginSubscribable(pluggableClass: Class<Pluggable>): Map<String, SubscribeSchema> {
      val classLoader = pluggableClass.classLoader
      val rootPackage = pluggableClass.`package`.name
      val configurationBuilder = ConfigurationBuilder()
        .forPackage(rootPackage, classLoader)
        .addScanners(Scanners.TypesAnnotated)

      val subscribableClasses = reflection(configurationBuilder)

      return subscribableClasses.map(this::parseSubscribableClass).associateBy { it.name }
    }

    private fun reflection(configurationBuilder: ConfigurationBuilder): List<Class<Subscribable>> {
      val reflections = Reflections(configurationBuilder)
      val subscribableClass = Subscribable::class.java
      return reflections.getTypesAnnotatedWith(Subscribe::class.java)
        .filter { !it.isInterface && subscribableClass.isAssignableFrom(it) }
        .map {
          @Suppress("UNCHECKED_CAST")
          it as Class<Subscribable>
        }
    }

    private fun parseSubscribableClass(subscribableClass: Class<Subscribable>): SubscribeSchema {
      val subscribeAnnotation = subscribableClass.getAnnotation(Subscribe::class.java)

      val eventMethods = ReflectionUtils.getAllMethods(subscribableClass, Predicate {
        it.isAnnotationPresent(SubscribeEvent::class.java)
      }).map {
        val eventAnnotation = it.getAnnotation(SubscribeEvent::class.java)
        val snakeName = eventAnnotation.name.ifBlank {
          Strings.snakeCase(it.name)
        }
        DataMethodSchema(it, snakeName)
      }.associateBy { it.method.toString() }

      return SubscribeSchema(
        subscribableClass,
        subscribeAnnotation.name,
        subscribeAnnotation.version,
        eventMethods
      )
    }
  }
}
