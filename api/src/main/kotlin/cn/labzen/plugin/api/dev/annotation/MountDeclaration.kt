package cn.labzen.plugin.api.dev.annotation

/**
 * 挂载组件的信息声明
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MountDeclaration(
  val name: String,
  val description: String
)
