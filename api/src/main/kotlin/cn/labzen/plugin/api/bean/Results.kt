package cn.labzen.plugin.api.bean

import cn.labzen.plugin.api.definition.ExtensibleInvokedStatus

data class Results internal constructor(
  val status: ExtensibleInvokedStatus,
  val values: Map<String, Value<*>>,
  val message: String? = null,
  val throwable: Throwable? = null
) {

  companion object {

    private val SUCCESS_EMPTY_RESULTS = Results(ExtensibleInvokedStatus.SUCCESS, emptyMap())
    private const val onlyOneValueName = "__<_no_name_value_>__"

    @JvmStatic
    @JvmOverloads
    fun empty(status: ExtensibleInvokedStatus? = null): Results =
      status?.let {
        Results(it, emptyMap())
      } ?: SUCCESS_EMPTY_RESULTS

    @JvmStatic
    @JvmOverloads
    fun <T> only(status: ExtensibleInvokedStatus? = null, value: T?): Results {
      val s = status ?: ExtensibleInvokedStatus.SUCCESS
      val values = mapOf(Pair(onlyOneValueName, Value(onlyOneValueName, value)))
      return Results(s, values)
    }

    @JvmStatic
    @JvmOverloads
    fun with(status: ExtensibleInvokedStatus? = null, vararg values: Value<*>): Results =
      with(status, values.toList())

    @JvmStatic
    @JvmOverloads
    fun with(status: ExtensibleInvokedStatus? = null, values: Collection<Value<*>>): Results {
      val s = status ?: ExtensibleInvokedStatus.SUCCESS
      val valueMap = values.associateBy { it.name }
      return Results(s, valueMap)
    }

    @JvmStatic
    @JvmOverloads
    fun throwing(message: String? = null, throwable: Throwable) =
      Results(ExtensibleInvokedStatus.EXCEPTION_THROWN, emptyMap(), message, throwable)

    @JvmStatic
    fun message(status: ExtensibleInvokedStatus, message: String) =
      Results(status, emptyMap(), message)
  }
}
