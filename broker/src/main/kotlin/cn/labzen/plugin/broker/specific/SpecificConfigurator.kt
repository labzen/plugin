package cn.labzen.plugin.broker.specific

import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.api.bean.Values
import cn.labzen.plugin.api.bean.schema.DataSchema
import cn.labzen.plugin.api.broker.Configurator
import cn.labzen.plugin.api.dev.Configurable
import cn.labzen.plugin.api.dev.Pluggable
import cn.labzen.plugin.api.dev.annotation.Configuration
import cn.labzen.plugin.api.dev.annotation.ConfigurationItem
import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.function.Predicate

class SpecificConfigurator internal constructor(
  private val pluggableClass: Class<Pluggable>
) : Configurator, MethodHandler {

  /**
   * key: 配置接口的方法名，value: 配置接口刚要
   */
  private val configurationSchemas = sortedMapOf<String, DataSchema>()

  /**
   * 配置在应用端的蛇形下划线风格命名
   */
  private lateinit var configurationValues: Values

  /**
   * key: 配置接口类，value: 配置接口代理类实例
   */
  private val configurationProxies = mutableMapOf<Class<*>, Any>()

  internal fun scanConfigurableInterfaces() {
    val classLoader = pluggableClass.classLoader
    val rootPackage = pluggableClass.`package`.name
    val configurationBuilder = ConfigurationBuilder()
      .forPackage(rootPackage, classLoader)
      .addScanners(Scanners.TypesAnnotated)
    val reflections = Reflections(configurationBuilder)

    val configurableClass = Configurable::class.java
    val configurableInterfaces = reflections.getTypesAnnotatedWith(Configuration::class.java)
      .filter { it.isInterface && configurableClass.isAssignableFrom(it) }
    configurableInterfaces.forEach(this::parseConfigurableInterface)
    configurableInterfaces.forEach(this::createConfigurationProxy)
  }

  private fun parseConfigurableInterface(configurableInterface: Class<*>) {
    val allMethods = ReflectionUtils.getAllMethods(configurableInterface, Predicate {
      it.isAnnotationPresent(ConfigurationItem::class.java)
    })/*.sortedWith(compareBy { it.name })*/

    val configurationAnnotation = configurableInterface.getAnnotation(Configuration::class.java)
    val namespace = configurationAnnotation.namespace
    val snakeNamespace = Strings.snakeCase(namespace)
    allMethods.forEach {
      val cleanName = it.name.removePrefix("get")
      val snakeItemName = Strings.snakeCase(cleanName)

      val type = it.returnType
      val annotation = it.getAnnotation(ConfigurationItem::class.java)
      val schema =
        DataSchema(
          "$snakeNamespace.$snakeItemName",
          type,
          annotation.description,
          annotation.require,
        )

      configurationSchemas[it.name] = schema
    }

    configurationValues = Values(configurationSchemas.values.toList())
  }

  private fun createConfigurationProxy(interfaceClass: Class<*>) {
    val proxyFactory = ProxyFactory()
    // 设置实现的接口
    proxyFactory.interfaces = arrayOf(interfaceClass)
    val proxyClass: Class<*> = proxyFactory.createClass()
    val javassistProxy = proxyClass.getDeclaredConstructor().newInstance()
    (javassistProxy as ProxyObject).handler = this

    configurationProxies[interfaceClass] = javassistProxy
  }

  internal fun getProxy(configurationClass: Class<*>?): Any =
    configurationProxies[configurationClass]
      ?: throw IllegalArgumentException("未找到合法的 @Configured 接口注入：$configurationClass")

  override fun invoke(self: Any, thisMethod: Method, proceed: Method?, args: Array<out Any>?): Any {
    val configurationSchema = configurationSchemas[thisMethod.name]
    return configurationSchema?.let {
      configurationValues.orElse(it.name)
    } ?: let {
      thisMethod.invoke(self, args)
    }
  }

  internal fun check() {
    configurationValues.validate()
  }

  override fun schema(): List<DataSchema> = configurationSchemas.values.toList()

  override fun configure(name: String, value: Any) {
    configurationValues[name] = value
  }
}
