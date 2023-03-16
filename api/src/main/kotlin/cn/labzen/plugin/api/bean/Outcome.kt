package cn.labzen.plugin.api.bean

data class Outcome internal constructor(
  val status: PluginOperateStatus,
  val values: Values? = null,
  val message: String? = null,
  val throwable: Throwable? = null
) {

  companion object {

    @JvmStatic
    fun empty(status: PluginOperateStatus): Outcome =
      Outcome(status)

    @JvmStatic
    @JvmOverloads
    fun success(values: Values? = null, message: String? = null): Outcome =
      Outcome(PluginOperateStatus.SUCCESS, values, message)

    @JvmStatic
    @JvmOverloads
    fun failed(message: String, values: Values? = null): Outcome =
      Outcome(PluginOperateStatus.FAILED, values, message)

    @JvmStatic
    @JvmOverloads
    fun throwing(message: String? = null, throwable: Throwable) =
      Outcome(PluginOperateStatus.EXCEPTION_THROWN, null, message, throwable)

    @JvmStatic
    fun message(status: PluginOperateStatus?, message: String) =
      Outcome(status ?: PluginOperateStatus.UNKNOWN, null, message)

    @JvmStatic
    @JvmOverloads
    fun with(status: PluginOperateStatus, values: Values, message: String? = null): Outcome =
      Outcome(status, values, message)
  }


  /**
   * 插件功能结果状态
   */
  enum class PluginOperateStatus {

    /**
     * 功能扩展正常执行
     */
    SUCCESS,

    /**
     * 功能扩展正常执行，但有警告产生，例如线上服务已登录，但有些功能因某些因素被受限访问
     */
    WARING,

    /**
     * 功能扩展执行时产生错误，意味着操作结果不受信任
     */
    FAILED,

    /**
     * 功能扩展时有异常抛出，导致未完全执行
     */
    EXCEPTION_THROWN,

    /**
     * 执行的功能为异步，等待插件主动通知上层应用（宿主）
     */
    WAITING,

    /**
     * 被忽略的操作，一般指功能扩展代码未被（完全）执行
     */
    IGNORE,

    UNKNOWN
  }
}
