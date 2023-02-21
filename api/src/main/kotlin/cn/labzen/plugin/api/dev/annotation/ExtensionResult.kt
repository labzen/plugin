// package cn.labzen.plugin.api.dev.annotation
//
// import kotlin.reflect.KClass
//
// /**
//  * 声明功能扩展服务执行后的结果信息，如不定义，不利于上层应用开发者使用，也无法生成详细的文档
//  */
// @MustBeDocumented
// @Target(AnnotationTarget.FIELD)
// @Retention(AnnotationRetention.RUNTIME)
// @Deprecated("通过outcome传递扩展执行后的返回参数，在@Extension.results中声明")
// annotation class ExtensionResult(
//   val name: String,
//   val type: KClass<Any>,
//   val description: String,
//   val required: Boolean = true
// )
