package cn.labzen.plugin.broker.event

import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.broker.specific.SpecificPublish
import cn.labzen.plugin.broker.specific.SpecificSubscribe
import java.lang.reflect.Method

internal object EventDispatcher {

  /**
   * 发布者实例集合
   *
   * key: 发布者类（发布者name必须为确切的名称）
   */
  private val publishSpecificsByClass = mutableMapOf<Class<out Publishable>, SpecificPublish>()

  /**
   * 订阅者实例集合
   *
   * key: 订阅者设置的"发布者name"，可能为正则表达式, value: 订阅者集合，订阅和发布的关系为 N:1
   */
  private val subscribeSpecificsByName = mutableMapOf<String, MutableSet<SpecificSubscribe>>()

  /**
   * 发布者和订阅者之间的订阅关系，对于更细一级的事件名称 event name 的匹配，将在发布时确定
   *
   * key: 确切的发布者 name, value: 订阅者设置的"发布者name"集合
   */
  private val publishAndSubscribePatternMap = mutableMapOf<String, MutableSet<String>>()

  @Synchronized
  fun registerPublish(publish: SpecificPublish) {
    publishSpecificsByClass[publish.schema.publishableClass] = publish

    val publisherName = publish.schema.name
    val subscriberNames = publishAndSubscribePatternMap.computeIfAbsent(publisherName) { mutableSetOf() }
    val matchedSubscriberNames = subscribeSpecificsByName.keys.filter { subscriberName ->
      publisherName == subscriberName || subscriberName == "*" || publisherName.matches(Regex(subscriberName))
    }

    subscriberNames.clear()
    subscriberNames.addAll(matchedSubscriberNames)
  }

  fun getPublish(cls: Class<out Publishable>) =
    publishSpecificsByClass[cls]

  @Synchronized
  fun registerSubscribe(subscribe: SpecificSubscribe) {
    val subscriberName = subscribe.schema.name
    val subscribes = subscribeSpecificsByName.computeIfAbsent(subscriberName) { mutableSetOf() }
    subscribes.add(subscribe)

    publishAndSubscribePatternMap.keys.forEach { publisherName ->
      if (publisherName == subscriberName || subscriberName == "*" || publisherName.matches(Regex(subscriberName))) {
        val subscriberNames = publishAndSubscribePatternMap.computeIfAbsent(publisherName) { mutableSetOf() }
        subscriberNames.add(subscriberName)
      }
    }
  }

  /**
   * @param publishMethod publish interface method
   * @param publisherName publishable name
   * @param publisherVersion publishable interface version
   * @param eventName event name
   * @param args event method arguments
   */
  fun eventPublished(
    publishMethod: Method,
    publisherName: String,
    publisherVersion: String,
    eventName: String,
    args: Array<out Any?>
  ) {
    val hittingSubscribes = mutableSetOf<SpecificSubscribe>()
    publishAndSubscribePatternMap[publisherName]?.forEach { subscriberName ->
      subscribeSpecificsByName[subscriberName]?.filter {
        it.schema.name == "*" || it.schema.version == publisherVersion
      }?.also {
        hittingSubscribes.addAll(it)
      }
    }

    var subscribeWithMapArguments: MutableMap<String, Any?>? = null
    hittingSubscribes.forEach { subscribe ->
      val eventMethods = subscribe.schema.events
      eventMethods.forEach { (en, dms) ->
        if (eventName == en) {
          dms.method.invoke(subscribe.instance, *args)
        } else if (en == "*" || eventName.matches(Regex(en))) {
          if (dms.method.parameterCount == 1 && Map::class.java.isAssignableFrom(dms.method.parameters[0].type)) {
            if (subscribeWithMapArguments == null) {
              subscribeWithMapArguments = mutableMapOf<String, Any?>().also {
                it["<<publisher name>>"] = publisherName
                it["<<publisher version>>"] = publisherVersion
                it["<<event name>>"] = eventName
                publishMethod.parameters.forEachIndexed { index, parameter ->
                  it[parameter.name] = args[index]
                }
              }
            }

            dms.method.invoke(subscribe.instance, subscribeWithMapArguments)
          }
        }
      }
    }
  }
}
