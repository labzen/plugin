package cn.labzen.plugin.broker.event

import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.api.bean.schema.DataMethodSchema
import cn.labzen.plugin.api.bean.schema.PublishSchema
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.api.event.annotation.Publish
import cn.labzen.plugin.api.event.annotation.PublishEvent
import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.function.Predicate

class SpecificPublish internal constructor(internal val schema: PublishSchema) : MethodHandler {

  internal val instance: Publishable = let {
    val proxyFactory = ProxyFactory()
    proxyFactory.interfaces = arrayOf(schema.publishableClass)
    val proxyClass: Class<*> = proxyFactory.createClass()
    val javassistProxy = proxyClass.getDeclaredConstructor().newInstance()
    (javassistProxy as ProxyObject).handler = this

    javassistProxy as Publishable
  }

  override fun invoke(self: Any, thisMethod: Method, proceed: Method, args: Array<out Any>?): Any? {
    val methodSchema = schema.events[thisMethod.toString()] ?: return null
    EventDispatcher.eventPublished(schema.name, schema.version, methodSchema.name, args)
    return null
  }

  companion object {

    internal fun scanPublishableInterfaces(pluggableClass: Class<Pluggable>): Map<String, PublishSchema> {
      val classLoader = pluggableClass.classLoader
      val rootPackage = pluggableClass.`package`.name
      val configurationBuilder = ConfigurationBuilder()
        .forPackage(rootPackage, classLoader)
        .addScanners(Scanners.TypesAnnotated)
      val reflections = Reflections(configurationBuilder)

      val publishableClass = Publishable::class.java
      val publishableClasses =
        reflections.getTypesAnnotatedWith(Publish::class.java)
          .filter { it.isInterface && publishableClass.isAssignableFrom(it) }
          .map {
            @Suppress("UNCHECKED_CAST")
            it as Class<Publishable>
          }

      return publishableClasses.map(this::parsePublishableClass).associateBy { it.name }
    }

    private fun parsePublishableClass(publishableClass: Class<Publishable>): PublishSchema {
      val publishAnnotation = publishableClass.getAnnotation(Publish::class.java)

      val eventMethods = ReflectionUtils.getAllMethods(publishableClass, Predicate {
        it.isAnnotationPresent(PublishEvent::class.java)
      }).map {
        val eventAnnotation = it.getAnnotation(PublishEvent::class.java)
        val snakeName = eventAnnotation.name.ifBlank {
          Strings.snakeCase(it.name)
        }
        DataMethodSchema(it, snakeName, eventAnnotation.description)
      }.associateBy { it.method.toString() }

      return PublishSchema(
        publishableClass,
        publishAnnotation.name,
        publishAnnotation.version,
        publishAnnotation.description,
        eventMethods
      )
    }
  }
}
