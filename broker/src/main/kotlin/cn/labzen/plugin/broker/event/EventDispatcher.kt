package cn.labzen.plugin.broker.event

import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.event.Publishable
import cn.labzen.plugin.broker.impl.specific.SpecificMount
import cn.labzen.plugin.broker.impl.specific.SpecificPublish
import cn.labzen.plugin.broker.impl.specific.SpecificSubscribe
import java.lang.reflect.Method

internal object EventDispatcher {

  /**
   * 发布者实例集合
   *
   * key: 发布者类（发布者name必须为确切的名称）
   */
  private val publishSpecificsByClass = mutableMapOf<Class<out Publishable<*>>, SpecificPublish>()

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

  fun getPublish(cls: Class<out Publishable<*>>) =
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
    eventPublishedWithMountable(null, publishMethod, publisherName, publisherVersion, eventName, args)
  }

  fun eventPublishedWithMountable(
    mountable: Mountable? = null,
    publishMethod: Method,
    publisherName: String,
    publisherVersion: String,
    eventName: String,
    args: Array<out Any?>
  ) {
    // 获取到事件发布相关的挂载组件实例，所对应的上层应用（宿主）具体挂载组件
    val mount = mountable?.let { SpecificMount.MOUNTABLE_REVERSE_INDEXES[it] }

    // 找到匹配发布者名称的所有订阅者
    val hittingSubscribes = mutableSetOf<SpecificSubscribe>()
    publishAndSubscribePatternMap[publisherName]?.forEach { subscriberName ->
      subscribeSpecificsByName[subscriberName]?.filter {
        it.schema.name == "*" || it.schema.version == publisherVersion
      }?.also {
        hittingSubscribes.addAll(it)
      }
    }

    hittingSubscribes.forEach {
      hitSubscribeEventMethod(it, publishMethod, eventName, publisherName, publisherVersion, mount, args)
    }
  }

  /**
   * 在订阅者内匹配相应的事件
   */
  private fun hitSubscribeEventMethod(
    subscribe: SpecificSubscribe,
    publishMethod: Method,
    publishEventName: String,
    publisherName: String,
    publisherVersion: String,
    mount: Mount?,
    args: Array<out Any?>
  ) {
    val eventMethods = subscribe.schema.events
    eventMethods.forEach { (subscribeEventName, subscribeMethodSchema) ->
      if (publishEventName == subscribeEventName) {
        try {
          invokeEventMethodWithExplicitName(publishMethod, subscribeMethodSchema.method, subscribe, mount, args)
        } catch (e: Exception) {
          // ignore
        }
      } else if (subscribeEventName == "*" || publishEventName.matches(Regex(subscribeEventName))) {
        // 如有使用 '*' 来匹配事件名的，参数转为Map
        val subscribeWithMapArguments = mutableMapOf<String, Any?>().also {
          it["<<publisher name>>"] = publisherName
          it["<<publisher version>>"] = publisherVersion
          it["<<event name>>"] = publishEventName
          publishMethod.parameters.forEachIndexed { index, parameter ->
            it[parameter.name] = args[index]
          }
        }

        try {
          invokeEventMethodWithInconclusiveName(
            publishMethod,
            subscribeMethodSchema.method,
            subscribe,
            mount,
            args,
            subscribeWithMapArguments
          )
        } catch (e: Exception) {
          // ignore
        }
      }

    }
  }

  /**
   * 明确的事件名
   */
  private fun invokeEventMethodWithExplicitName(
    publishMethod: Method,
    subscribeMethod: Method,
    subscribe: SpecificSubscribe,
    mount: Mount?,
    args: Array<out Any?>
  ) {
    val hasMountArgument = if (subscribeMethod.parameterCount > 0) {
      subscribeMethod.parameterTypes[0] == Mount::class.java
    } else false
    val arguments = if (hasMountArgument) {
      arrayOf(mount, *args)
    } else args

    try {
      subscribeMethod.invoke(subscribe.instance, *arguments)
    } catch (e: IllegalArgumentException) {
      tryInvokeEventMethodByPreciseArguments(publishMethod, subscribeMethod, subscribe, mount, args)
    }
  }

  /**
   * 通过 '*' 或 使用正则表达式匹配事件名
   */
  private fun invokeEventMethodWithInconclusiveName(
    publishMethod: Method,
    subscribeMethod: Method,
    subscribe: SpecificSubscribe,
    mount: Mount?,
    args: Array<out Any?>,
    mapArguments: MutableMap<String, Any?>
  ) {
    if (subscribeMethod.parameterCount == 1) {
      val firstArgumentType = subscribeMethod.parameterTypes[0]
      if (Map::class.java.isAssignableFrom(firstArgumentType)) {
        // 使用 Map 参数接收，一般用于正则表达式或'*'通配符匹配事件名的情况
        subscribeMethod.invoke(subscribe.instance, mapArguments)
        return
      } else if (Mount::class.java == firstArgumentType) {
        // 只接收发布事件时的关联挂载组件，虽然可以，但没啥意义
        subscribeMethod.invoke(subscribe.instance, mount)
        return
      }
    } else if (subscribeMethod.parameterCount == 2) {
      if (Mount::class.java == subscribeMethod.parameterTypes[0] &&
        Map::class.java.isAssignableFrom(subscribeMethod.parameterTypes[1])
      ) {
        subscribeMethod.invoke(subscribe.instance, mount, mapArguments)
        return
      }
    }

    tryInvokeEventMethodByPreciseArguments(publishMethod, subscribeMethod, subscribe, mount, args)
  }

  private fun tryInvokeEventMethodByPreciseArguments(
    publishMethod: Method,
    subscribeMethod: Method,
    subscribe: SpecificSubscribe,
    mount: Mount?,
    args: Array<out Any?>
  ) {
    val hasMountArgument = if (subscribeMethod.parameterCount > 0) {
      subscribeMethod.parameterTypes[0] == Mount::class.java
    } else false
    var subscribeMethodArgumentStep = if (hasMountArgument) 1 else 0

    publishMethod.parameterTypes.forEach { publishArgumentType ->
      if (subscribeMethodArgumentStep >= subscribeMethod.parameterCount) {
        return
      }

      val subscribeArgumentType = subscribeMethod.parameterTypes[subscribeMethodArgumentStep++]
      if (subscribeArgumentType != publishArgumentType) {
        return
      }
    }

    val arguments = if (hasMountArgument) {
      arrayOf(mount, *args)
    } else args

    subscribeMethod.invoke(subscribe.instance, *arguments)
  }

}
