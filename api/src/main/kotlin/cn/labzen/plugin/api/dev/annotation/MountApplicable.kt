// package cn.labzen.plugin.api.dev.annotation
//
// import cn.labzen.plugin.api.dev.Mountable
// import kotlin.reflect.KClass
//
// /**
//  * 标记扩展服务适用于哪一个挂载组件，如果不注解，则认为扩展服务适用于全局，即与挂载组件无关
//  */
// @MustBeDocumented
// @Target(AnnotationTarget.CLASS)
// @Retention(AnnotationRetention.RUNTIME)
// annotation class MountApplicable(
//   val mount: KClass<out Mountable>
// )
