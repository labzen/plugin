// package cn.labzen.plugin.api.dev.core
//
// import cn.labzen.cells.core.kotlin.initOnce
// import cn.labzen.plugin.api.dev.Configurable
//
// object Plugins : PluginsPlayer {
//
//   private val player = initOnce<PluginsPlayer>()
//
//   override fun <C : Configurable> configuration(configurableInterface: Class<C>): C =
//     player.get().configuration(configurableInterface)
//
// }
