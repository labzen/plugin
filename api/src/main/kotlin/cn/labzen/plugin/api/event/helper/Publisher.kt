package cn.labzen.plugin.api.event.helper

import cn.labzen.plugin.api.event.Publishable
import java.util.*

/**
 * 插件可能会在任何时间、条件下需要发布事件，通知上层应用事件信息，提供本类以方便在任何类中获取到
 */
object Publisher {

  private lateinit var provider: PublisherProvider

  /**
   * 随时获取插件时间发布器实例
   */
  @JvmStatic
  fun <P : Publishable> instance(publishable: Class<out Publishable>): Optional<P> =
    // 只是个壳，具体实现在broker中
    Optional.ofNullable(provider.getPublisherInstance(publishable))

  interface PublisherProvider {

    fun <P : Publishable> getPublisherInstance(publishableClass: Class<out Publishable>): P?
  }
}
