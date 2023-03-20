package cn.labzen.plugin.broker.impl.memoir.bean

enum class PluginContextStatus {

  /**
   * 插件已加载入JVM
   */
  LOADED,

  /**
   * 插件已准备好启动（激活）
   */
  ACTIVATE_PREPARED,

  /**
   * 插件已经激动（激活）
   */
  ACTIVATED,

  /**
   * 插件已经准备好停止（失活）
   */
  INACTIVATE_PREPARED,

  /**
   * 插件已经停止（失活）
   */
  INACTIVATED
}
