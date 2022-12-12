package cn.labzen.plugin.broker.resource

import java.net.URL

interface ResourceLoader {

  /**
   * 唯一资源定位符
   */
  fun getUrl(): URL

  /**
   * 所依赖的其他资源
   */
  fun associates(): List<URL>
}
