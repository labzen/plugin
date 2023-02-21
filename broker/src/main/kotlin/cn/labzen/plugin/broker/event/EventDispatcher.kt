package cn.labzen.plugin.broker.event

import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.broker.specific.SpecificPublish
import cn.labzen.plugin.broker.specific.SpecificSubscribe

internal object EventDispatcher {

  private val publishSpecificsByName = mutableMapOf<String, SpecificPublish>()
  private val publishSpecificsByClass = mutableMapOf<Class<out Publishable>, SpecificPublish>()

  private val subscribeSpecificsByName = mutableMapOf<String, MutableSet<SpecificSubscribe>>()

  fun registerPublish(publish: SpecificPublish) {
    publishSpecificsByName[publish.schema.name] = publish
    publishSpecificsByClass[publish.schema.publishableClass] = publish
  }

  fun getPublish(cls: Class<out Publishable>) =
    publishSpecificsByClass[cls]

  fun registerSubscribe(subscribe: SpecificSubscribe) {
    val subscribes = subscribeSpecificsByName.computeIfAbsent(subscribe.schema.name) { mutableSetOf() }
    subscribes.add(subscribe)
  }

  fun eventPublished(name: String, version: String, eventMethodName: String, args: Array<out Any>?) {
    val subscribes = subscribeSpecificsByName[name]?.filter {
      it.schema.version == version
    } ?: return

    subscribes.forEach { subscribe ->
      val methodSchema = subscribe.schema.events[eventMethodName]
      methodSchema?.apply {
        // todo 可能出现参数 args 的顺序或个数、类型等问题，这里需要验证
        this.method.invoke(subscribe.instance, args)
      }
    }
  }
}
