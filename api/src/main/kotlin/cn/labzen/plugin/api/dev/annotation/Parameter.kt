package cn.labzen.plugin.api.dev.annotation

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Parameter(
  val description: String,
  val required: Boolean = true
)
