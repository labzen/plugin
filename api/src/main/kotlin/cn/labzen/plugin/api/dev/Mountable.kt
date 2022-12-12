package cn.labzen.plugin.api.dev

/**
 * 一个挂载组件（部件）接口，标识一个具体的单位（硬件或账号等），用于精确表示扩展服务的功用对象
 */
interface Mountable {

  /**
   * 当挂载可挂载组件时调用
   */
  fun onMounted()

  /**
   * 当要卸载可挂载组件前调用
   */
  fun onUnmount()
}
