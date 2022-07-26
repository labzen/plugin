package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.bean.Value

/**
 * 插件实际功能扩展
 */
interface Extensible {

  /**
   * 扩展功能被执行时调用
   */
  fun execute()
}
