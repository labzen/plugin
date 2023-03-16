package cn.labzen.plugin.broker.event

import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.api.event.helper.Publisher

internal object PublisherDelegator : Publisher.PublisherProvider {

  internal fun initialize() {
    val providerField = Publisher::class.java.getDeclaredField("provider")
    providerField.isAccessible = true
    providerField.set(Publisher, this)
  }

  @Suppress("UNCHECKED_CAST")
  override fun <P : Publishable<*>> getPublisherInstance(publishableClass: Class<out Publishable<*>>): P? =
    EventDispatcher.getPublish(publishableClass)?.instance as P?
}
