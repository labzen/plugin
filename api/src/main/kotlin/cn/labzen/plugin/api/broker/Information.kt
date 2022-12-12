package cn.labzen.plugin.api.broker

interface Information {

  /**
   * 插件的名称
   */
  fun name(): String

  /**
   * 插件版本
   */
  fun version(): String

  /**
   * 插件的功能详细描述
   */
  fun description(): String

  /**
   * 插件标签列表
   */
  fun tags(): List<String>

  /**
   * 插件作者列表
   */
  fun authors(): List<Pair<String, String>>

  /**
   * 插件的Changelog
   */
  fun changelogs(): List<Pair<String, List<String>>>
}
