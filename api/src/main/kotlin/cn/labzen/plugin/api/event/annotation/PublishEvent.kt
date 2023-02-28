package cn.labzen.plugin.api.event.annotation

import cn.labzen.plugin.api.dev.Mountable
import cn.labzen.plugin.api.dev.nop.NopMountable
import kotlin.reflect.KClass

/**
 * 声明插件主动发布的内容信息详情
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PublishEvent(
  val name: String = "",
  val mountable: KClass<out Mountable> = NopMountable::class,
  val description: String
)
