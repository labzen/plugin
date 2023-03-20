package cn.labzen.plugin.api.dev

import cn.labzen.plugin.api.broker.Mount

/**
 * 一个挂载组件（部件）接口，标识一个具体的单位（硬件或账号等），用于精确表示扩展服务的功用对象
 */
interface Mountable {

  /**
   * 当挂载可挂载组件时调用
   *
   * @param identifier 挂载实例符号，用于唯一标识一个挂载组件，参考[Mount]
   */
  fun onMounted(identifier: String)

  /**
   * 当要卸载可挂载组件前调用
   */
  fun onUnmounting()
}
