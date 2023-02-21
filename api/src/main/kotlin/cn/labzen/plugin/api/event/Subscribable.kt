package cn.labzen.plugin.api.event

/**
 * 插件事件订阅，订阅事件内容
 */
interface Subscribable {

  /**
   * TODO(当订阅的事件接受失败时调用)
   */
  fun onFailed()
}
