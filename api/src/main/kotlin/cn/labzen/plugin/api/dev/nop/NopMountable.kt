package cn.labzen.plugin.api.dev.nop

import cn.labzen.plugin.api.dev.Mountable

class NopMountable : Mountable {

  override fun onMounted(identifier: String) {
    // do nothing
  }

  override fun onUnmounting() {
    // do nothing
  }
}
