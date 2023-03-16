package cn.labzen.plugin.broker.meta

import cn.labzen.meta.configuration.annotation.Configured
import cn.labzen.meta.configuration.annotation.Item
import org.slf4j.event.Level

@Configured("plugin.broker")
interface PluginBrokerConfiguration {

  /**
   * 应用层的项目根路径地址
   */
  @Item(path = "root", logLevel = Level.INFO)
  fun applicationPackage(): String

  /**
   * 指定Maven中间件的HOME地址
   */
  @Item(path = "maven.home", logLevel = Level.INFO)
  fun mavenHome(): String

  /**
   * Maven本地仓库位置
   */
  @Item(path = "maven.repository.local", logLevel = Level.INFO, required = false)
  fun mavenLocalRepositoryLocation(): String?

  /**
   * Maven远程仓库地址
   */
  @Item(path = "maven.repository.remote", defaultValue = "https://maven.aliyun.com/repository/public", required = false)
  fun mavenRemoteRepositoryUri(): String

  /**
   * Maven插件org.apache.maven.plugins:maven-dependency-plugin的版本
   */
  @Item(path = "maven.plugin.dependency.version", defaultValue = "3.2.0", required = false)
  fun mavenPluginDependencyVersion(): String

  /**
   * 是否启动插件的回忆录机制
   */
  @Item(path = "memoir.enable", defaultValue = "false", logLevel = Level.INFO)
  fun memoirEnable(): Boolean

  /**
   * 回忆录机制的存储位置
   */
  @Item(path = "memoir.root", logLevel = Level.INFO, required = false)
  fun memoirStorePath(): String?

  /**
   * 回忆录机制下，插件对应的文件加密秘钥
   */
  @Item(path = "memoir.secret-key", logLevel = Level.INFO, required = false)
  fun memoirSecretKey(): String?
}
