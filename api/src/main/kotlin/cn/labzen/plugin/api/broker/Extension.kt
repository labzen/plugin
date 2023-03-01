package cn.labzen.plugin.api.broker

import cn.labzen.plugin.api.bean.Outcome

interface Extension {

  /**
   * 设置服务扩展所需参数
   */
  fun setParameter(name: String, value: Any?)

  /**
   * 调起服务扩展执行
   */
  fun invoke(): Outcome

  /**
   * 关停扩展服务
   */
  fun destructing()
}
