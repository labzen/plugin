package cn.labzen.plugin.api.bean

import com.google.common.base.Supplier
import java.util.function.Consumer

data class Value<T>(val name: String, val original: T?) {

  fun isPresent(): Boolean =
    original != null

  @Throws(NoSuchElementException::class)
  fun get(): T =
    original ?: throw NoSuchElementException("")

  fun orElse(other: T): T =
    original ?: other

  fun orElse(supplier: Supplier<T>): T =
    original ?: supplier.get()

  fun orThrow(supplier: Supplier<RuntimeException>): T =
    original ?: throw supplier.get()

  fun ifPresent(consumer: Consumer<T>) {
    original ?: return
    consumer.accept(original)
  }

  fun copy(fresh: T?) =
    copy(name = name, original = fresh)
}
