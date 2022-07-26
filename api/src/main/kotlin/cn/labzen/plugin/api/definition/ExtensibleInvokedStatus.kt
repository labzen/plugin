package cn.labzen.plugin.api.definition

/**
 * 插件功能扩展的执行结果状态
 */
enum class ExtensibleInvokedStatus {

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
   * 执行的功能为异步，等待插件主动通知上层应用
   */
  WAITING,

  /**
   * 被忽略的操作，一般指功能扩展代码未被（完全）执行
   */
  IGNORE
}
