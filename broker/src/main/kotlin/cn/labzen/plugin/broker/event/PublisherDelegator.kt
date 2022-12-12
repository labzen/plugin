package cn.labzen.plugin.broker.event

import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.api.event.helper.Publisher

// internal object PublisherDelegator : Function<Class<out Publishable>, Publishable?> {
internal object PublisherDelegator : Publisher.PublisherProvider {

  init {
    // val proxyFactory = ProxyFactory()
    // proxyFactory.interfaces = arrayOf(Function::class.java)
    // proxyFactory.setFilter { it.name == "apply" }
    // val proxyClass: Class<*> = proxyFactory.createClass()
    // val javassistProxy = proxyClass.getDeclaredConstructor().newInstance()
    // (javassistProxy as ProxyObject).handler = PublisherDelegator

    // @Suppress("UNCHECKED_CAST")
    // val publishProvider = javassistProxy as Function<Class<Publishable>, Publishable>
    val providerField = Publisher::class.java.getDeclaredField("provider")
    providerField.isAccessible = true
    providerField.set(Publisher, this)
  }

  // override fun apply(publishableClass: Class<out Publishable>): Publishable? =
  //   publisherInstances[publishableClass]?.instance

  @Suppress("UNCHECKED_CAST")
  override fun <P : Publishable> getPublisherInstance(publishableClass: Class<out Publishable>): P? =
    EventDispatcher.getPublish(publishableClass)?.instance as P?

  // override fun invoke(self: Any, thisMethod: Method, proceed: Method, args: Array<out Any>?): Any? {
  //   val publishableClass = args!![0]
  //   return publisherInstances[publishableClass]
  // }
}
