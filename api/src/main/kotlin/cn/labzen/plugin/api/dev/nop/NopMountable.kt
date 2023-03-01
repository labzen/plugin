package cn.labzen.plugin.api.dev.nop

import cn.labzen.plugin.api.dev.Mountable

class NopMountable : Mountable {

  override fun onMounted(symbol: String) {
    // nothing
  }

  override fun onUnmounting() {
    // nothing
  }
}
