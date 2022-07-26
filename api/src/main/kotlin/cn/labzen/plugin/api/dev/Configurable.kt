package cn.labzen.plugin.api.dev

/**
 * 插件的可配置类
 */
interface Configurable {

  /**
   * 当插件加载成功后，激活之前，第一时间传入配置
   */
  fun configured()
}
