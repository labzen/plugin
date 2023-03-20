package cn.labzen.plugin.broker.impl.handler

import cn.labzen.plugin.api.broker.Mount
import cn.labzen.plugin.api.broker.Plugin
import cn.labzen.plugin.broker.javassist.JavassistUtil
import javassist.util.proxy.MethodHandler
import java.lang.reflect.Method

/**
 * 插件代理处理器，提供部分插件功能的拦截做进一步功能增强
 */
internal class PluginProxyHandler(private val plugin: Plugin) : MethodHandler {

  override fun invoke(self: Any?, thisMethod: Method, proceed: Method?, args: Array<out Any?>): Any? {
    return when (thisMethod.name) {
      "mounting" -> {
        val mount = thisMethod.invoke(plugin, *args) as Mount
        JavassistUtil.createProxyImplements(MountProxyHandler(mount), Mount::class.java)
      }

      "inactivating" -> {
        MountInstanceHolder.mounted().forEach {
          it.unmounting()
        }
        thisMethod.invoke(plugin)
      }

      else -> thisMethod.invoke(plugin, *args)
    }
  }
}
