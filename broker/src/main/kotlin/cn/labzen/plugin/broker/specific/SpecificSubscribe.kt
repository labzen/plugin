package cn.labzen.plugin.broker.specific

import cn.labzen.meta.Labzens
import cn.labzen.plugin.api.bean.schema.DataMethodSchema
import cn.labzen.plugin.api.bean.schema.SubscribeSchema
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

// to!do: 发布时，可以带有挂载物的标识，需要考虑创建挂载物时生成标识，并让宿主得知
// fixme: 如果存在不同名称的多个发布者，并且订阅者订阅的是 *，发布者中存在重命的事件，而且事件参数不同，会发生找不到正确的订阅方法问题（通知不到）
class SpecificSubscribe internal constructor(internal val schema: SubscribeSchema) {

  internal val instance = Springs.getOrCreate(schema.subscribableClass)

  companion object {

    /**
     * 扫描在上层应用中的订阅
     */
    internal fun scanApplicationSubscribable(): Map<String, SubscribeSchema> {
      val configuration = Labzens.configurationWith(PluginBrokerConfiguration::class.java)

      val configurationBuilder = ConfigurationBuilder()
        .forPackages(configuration.applicationPackage())
        .addScanners(Scanners.TypesAnnotated)

      val reflections = Reflections(configurationBuilder)
      val subscribableClass = Subscribable::class.java
      val subscribableClasses = reflections.getTypesAnnotatedWith(Subscribe::class.java)
        .filter { !it.isInterface && subscribableClass.isAssignableFrom(it) }
        .map {
          @Suppress("UNCHECKED_CAST")
          it as Class<Subscribable>
        }

      return subscribableClasses.map(this::parseSubscribableClass).associateBy { it.name }
      // todo 扫描每个类中订阅的具体事件方法，跟发布specific一样，把两边的对应关系放到一个单独的代理类中处理
    }

    /**
     * 扫描在当前加载插件中的订阅
     */
    internal fun scanPluginSubscribable(classes: List<Class<*>>): Map<String, SubscribeSchema> {
      @Suppress("UNCHECKED_CAST")
      val subscribableClasses = classes as List<Class<Subscribable>>
      return subscribableClasses.map(this::parseSubscribableClass).associateBy { it.name }
    }

    private fun parseSubscribableClass(subscribableClass: Class<Subscribable>): SubscribeSchema {
      val subscribeAnnotation = subscribableClass.getAnnotation(Subscribe::class.java)

      val eventMethods = ReflectionUtils.getAllMethods(subscribableClass, Predicate {
        it.isAnnotationPresent(SubscribeEvent::class.java)
      }).map {
        val eventAnnotation = it.getAnnotation(SubscribeEvent::class.java)
        val eventName = eventAnnotation.name.ifBlank { it.name }
        DataMethodSchema(it, eventName)
      }.associateBy { it.name }

      return SubscribeSchema(
        subscribableClass,
        subscribeAnnotation.name,
        subscribeAnnotation.version,
        eventMethods
      )
    }
  }
}
