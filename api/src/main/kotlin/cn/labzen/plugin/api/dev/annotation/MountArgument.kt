package cn.labzen.plugin.api.dev.annotation

/**
 * 声明可挂载组件的参数
 */
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MountArgument(
  val description: String,
  val required: Boolean = true
)
