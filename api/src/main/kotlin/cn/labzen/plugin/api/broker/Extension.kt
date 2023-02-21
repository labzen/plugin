package cn.labzen.plugin.api.broker

import cn.labzen.plugin.api.bean.Outcome

interface Extension {

  fun setParameter(name: String, value: Any?)

  fun invoke(): Outcome
}
