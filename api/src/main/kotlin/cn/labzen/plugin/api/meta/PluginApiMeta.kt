package cn.labzen.plugin.api.meta

import cn.labzen.meta.component.LabzenComponent

class PluginApiMeta : LabzenComponent {

  override fun description(): String =
    "插件机制底层API支撑"

  override fun mark(): String =
    "Labzen-Plugin-API"

  override fun packageBased(): String =
    "cn.labzen.plugin.api"
}
