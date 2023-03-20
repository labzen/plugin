package cn.labzen.plugin.broker.impl.handler

import cn.labzen.plugin.api.broker.Mount
import javassist.util.proxy.MethodHandler
import java.lang.reflect.Method

/**
 * 挂载组件代理处理器，提供部分挂载组件功能的拦截做进一步功能增强
 */
class MountProxyHandler(private val mount: Mount) : MethodHandler {

  override fun invoke(self: Any?, thisMethod: Method, proceed: Method?, args: Array<out Any?>): Any? {
    return when (thisMethod.name) {
      "done" -> {
        thisMethod.invoke(mount, *args)
        MountInstanceHolder.mounting(self!! as Mount)
      }

      "unmounting" -> {
        thisMethod.invoke(mount, *args)
        MountInstanceHolder.unmounting(self!! as Mount)
      }

      else -> thisMethod.invoke(mount, *args)
    }
  }
}
