package cn.labzen.plugin.broker.impl.handler

import cn.labzen.plugin.api.broker.Mount

internal object MountInstanceHolder {

  private val allMounted = mutableMapOf<String, Mount>()

  fun mounting(proxiedMount: Mount) {
    allMounted[proxiedMount.identifier()] = proxiedMount
  }

  fun unmounting(proxiedMount: Mount) {
    allMounted.remove(proxiedMount.identifier())
  }

  internal fun mounted(): List<Mount> = allMounted.values.toList()
  internal fun mounted(identifier: String): Mount? = allMounted[identifier]
}
