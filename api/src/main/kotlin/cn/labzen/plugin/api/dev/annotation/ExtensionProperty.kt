package cn.labzen.plugin.api.dev.annotation

import kotlin.reflect.KClass

/**
 * 声明功能扩展服务的的属性信息
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExtensionProperty(
  val name: String = "",
  val type: KClass<out Any> = Any::class,
  val description: String,
  val required: Boolean = true
)
