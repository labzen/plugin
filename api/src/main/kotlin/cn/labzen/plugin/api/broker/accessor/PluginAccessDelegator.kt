package cn.labzen.plugin.api.broker.accessor

import cn.labzen.plugin.api.broker.Information
import cn.labzen.plugin.api.broker.Mount
import java.util.*

interface PluginAccessDelegator {
  fun plugin(): LimitedAccessPlugin
  fun information(): Information
  fun configuration(): Map<String, Any?>
  fun mounted(): List<Mount>
  fun mounted(identifier: String): Optional<Mount>
}
