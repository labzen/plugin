package cn.labzen.plugin.api.bean

import com.google.common.base.Supplier
import org.springframework.beans.BeanUtils
import java.util.function.Consumer

class TransmittableValues(private val originalBean: Any) : Values() {

  private val propertyDescriptors = BeanUtils.getPropertyDescriptors(originalBean.javaClass).associateBy {
    it.name
  }.filter { it.key != "class" }

  override fun isPresent(name: String): Boolean =
    get(name) != null

  override fun ifPresent(name: String, consumer: Consumer<Any>) {
    get(name)?.apply { consumer.accept(this) }
  }

  override fun get(name: String): Any? =
    propertyDescriptors[name]?.readMethod?.invoke(originalBean)

  override fun <T : Any> whole(targetClass: Class<T>): T {
    val targetBean = targetClass.getDeclaredConstructor().newInstance()
    BeanUtils.copyProperties(originalBean, targetBean)
    return targetBean
  }

  override fun whole(): Map<String, Any?> =
    propertyDescriptors.mapValues {
      it.value.readMethod.invoke(originalBean)
    }

  override fun orElse(name: String, other: Any?): Any? =
    get(name) ?: other

  override fun orElse(name: String, supplier: Supplier<Any?>): Any? =
    get(name) ?: supplier.get()

  override fun orThrow(name: String, supplier: Supplier<RuntimeException>): Any =
    get(name) ?: throw supplier.get()

}
