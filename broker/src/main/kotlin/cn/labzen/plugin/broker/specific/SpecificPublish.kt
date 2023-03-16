package cn.labzen.plugin.broker.specific

import cn.labzen.plugin.api.bean.schema.EventSchema
import cn.labzen.plugin.api.bean.schema.PublishSchema
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.nop.NopMountable
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

  internal val instance: Publishable<*> = let {
    val javassistProxy = createProxy()
    (javassistProxy as ProxyObject).handler = this

    javassistProxy as Publishable<*>
  }

  private fun createProxy(): Any? {
    val proxyFactory = ProxyFactory()
    proxyFactory.interfaces = arrayOf(schema.publishableClass)
    val proxyClass: Class<*> = proxyFactory.createClass()
    return proxyClass.getDeclaredConstructor().newInstance()
  }

  override fun invoke(self: Any?, thisMethod: Method, proceed: Method?, args: Array<out Any?>): Any? {
    // 调用 withMount 方法
    if ("withMount" == thisMethod.name &&
      thisMethod.parameterCount == 1 &&
      thisMethod.parameterTypes[0] == Mountable::class.java
    ) {
      return createWithMountPublishableProxy(args[0] as Mountable)
    }

    val methodSchema = schema.events[thisMethod.toString()] ?: return null
    EventDispatcher.eventPublished(thisMethod, schema.name, schema.version, methodSchema.name, args)
    return null
  }

  private fun createWithMountPublishableProxy(mountable: Mountable): Any {
    val javassistProxy = createProxy()
    (javassistProxy as ProxyObject).handler = WithMountPublishable(mountable, schema)
    return javassistProxy
  }

  class WithMountPublishable(private val mountable: Mountable, private val schema: PublishSchema) : MethodHandler {

    override fun invoke(self: Any?, thisMethod: Method, proceed: Method?, args: Array<out Any?>): Any? {
      val methodSchema = schema.events[thisMethod.toString()] ?: return null
      EventDispatcher.eventPublishedWithMountable(
        mountable, thisMethod, schema.name, schema.version, methodSchema.name, args
      )
      return null
    }
  }

  companion object {

    internal fun scanPublishableInterfaces(classes: List<Class<*>>): Map<String, PublishSchema> {
      @Suppress("UNCHECKED_CAST")
      val publishableClasses = classes as List<Class<Publishable<*>>>
      return publishableClasses.map(this::parsePublishableClass).associateBy { it.name }
    }

    private fun parsePublishableClass(publishableClass: Class<Publishable<*>>): PublishSchema {
      val publishAnnotation = publishableClass.getAnnotation(Publish::class.java)

      val eventMethods = ReflectionUtils.getAllMethods(publishableClass, Predicate {
        it.isAnnotationPresent(PublishEvent::class.java)
      }).map {
        val eventAnnotation = it.getAnnotation(PublishEvent::class.java)
        val eventName = eventAnnotation.name.ifBlank { it.name }
        val mountableClass =
          if (eventAnnotation.mountable == NopMountable::class) null else eventAnnotation.mountable.java
        EventSchema(it, eventName, mountableClass, eventAnnotation.description)
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
