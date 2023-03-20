package cn.labzen.plugin.broker.javassist

import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject

@Suppress("DuplicatedCode")
object JavassistUtil {

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> createProxyImplements(accessibleHandler: MethodHandler, vararg types: Class<T>): T {
    val proxyFactory = ProxyFactory()
    proxyFactory.interfaces = types
    val proxyClass: Class<T> = proxyFactory.createClass() as Class<T>
    val proxyInstance = proxyClass.getDeclaredConstructor().newInstance()
    (proxyInstance as ProxyObject).handler = accessibleHandler
    return proxyInstance
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> createProxyExtends(accessibleHandler: MethodHandler, type: Class<T>): T {
    val proxyFactory = ProxyFactory()
    proxyFactory.superclass = type
    val proxyClass: Class<T> = proxyFactory.createClass() as Class<T>
    val proxyInstance = proxyClass.getDeclaredConstructor().newInstance()
    (proxyInstance as ProxyObject).handler = accessibleHandler
    return proxyInstance
  }
}
