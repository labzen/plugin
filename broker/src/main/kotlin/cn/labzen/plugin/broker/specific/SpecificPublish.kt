package cn.labzen.plugin.broker.specific

import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.api.bean.schema.DataMethodSchema
import cn.labzen.plugin.api.bean.schema.PublishSchema
import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.api.event.annotation.Publish
import cn.labzen.plugin.api.event.annotation.PublishEvent
import cn.labzen.plugin.broker.event.EventDispatcher
import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.reflections.ReflectionUtils
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

    internal fun scanPublishableInterfaces(classes: List<Class<*>>): Map<String, PublishSchema> {
      @Suppress("UNCHECKED_CAST")
      val publishableClasses = classes as List<Class<Publishable>>
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
