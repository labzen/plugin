package cn.labzen.plugin.api.dev.annotation

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Return(
  val description: String,
  val required: Boolean = true
)
