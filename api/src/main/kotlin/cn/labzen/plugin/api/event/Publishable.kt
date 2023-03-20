package cn.labzen.plugin.api.event

import cn.labzen.plugin.api.dev.Mountable

/**
 * 插件事件发布接口，主动发布事件内容
 */
interface Publishable<out P : Publishable<P>> {

  /**
   * 发布的时间与某挂载组件相关
   */
  fun withMount(mountable: Mountable): P
}
