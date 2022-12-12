package cn.labzen.plugin.broker.maven

import cn.labzen.cells.core.utils.Strings
import cn.labzen.logger.kotlin.logger
import cn.labzen.meta.Labzens
import cn.labzen.plugin.broker.exception.PluginMavenException
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.invoker.*
import java.io.BufferedReader
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.URL

object Mavens {

  private const val NEXUS_REPOSITORY_URI = "https://maven.aliyun.com/repository/public"
  private const val DEPENDENCY_GET_COMMAND = "org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get" +
      " -DremoteRepositories=aliyun::default::$NEXUS_REPOSITORY_URI -Dartifact={} -Dpackaging={}"

  private val logger = logger { }
  private val tempDirectory: File = File(Labzens.environment().ioTempPath)
  private val localRepositoryRoot = File(Labzens.environment().userHome, ".m2${File.separator}repository")

  const val MAVEN_JAR_FILE_EXTENSION = ".jar"
  const val MAVEN_POM_FILE_EXTENSION = ".pom"
  const val MAVEN_XML_FILE_EXTENSION = ".xml"
  const val MAVEN_POM_XML_FILE = "pom.xml"

  init {
    // todo 验证本机是否有可用Maven，以及版本
    logger.info("Plugin Broker准备使用默认Maven本地仓库地址：{}", localRepositoryRoot.absoluteFile)

    if (!localRepositoryRoot.exists()) {
      logger.warn("Plugin Broker找不到默认Maven本地仓库目录")
      // 使用maven命令 mvn help:effective-settings 获取可用本地仓库地址，用时过长，考虑使用配置的方式传入
      // todo 使用配置的方式传入本地仓库地址
      throw PluginMavenException("未找到")
      // logger.info("Plugin Broker将创建该默认Maven本地仓库目录：{}", localRepositoryRoot.absoluteFile)
      // localRepositoryRoot.mkdirs()
    }
  }

  // fun file(coordinate: String): File? {
  //   val artifact = Artifact.parseWithCoordinate(coordinate)
  //   artifact.downloadIfNecessary()
  //   return artifact.localFile
  // }

  /**
   * 解析Maven坐标为[Artifact]类
   *
   * 坐标格式为 `'groupId:artifactId:version'` 或 `'groupId:artifactId:packaging:version'`
   */
  fun parseCoordinate(coordinate: String): Artifact {
    val chips = coordinate.split(":")

    if (chips.size < 3 || chips.size > 4) {
      throw PluginMavenException("非法的Maven资源坐标：{}", coordinate)
    }

    val groupId = chips[0]
    val artifactId = chips[1]
    val version = chips.last()
    val packaging = if (chips.size == 4) {
      val ps = chips[2]
      Artifact.Packaging.parse(ps)
    } else Artifact.Packaging.JAR

    return Artifact(groupId, artifactId, version, packaging)
  }

  fun coordinateString(groupId: String, artifactId: String, version: String): String =
    "$groupId:$artifactId:$version"

  fun parsePomContentToArtifact(content: String, originalPomFile: URL? = null): Artifact {
    val model = parsePomModel(content)
    val packaging = Artifact.Packaging.parse(model.packaging)
    return Artifact(model.artifactId, model.groupId, model.version, packaging, originalPomFile)
  }

  /**
   * 将工件信息转换为相对于本地仓库根目录的工具文件资源相对路径
   */
  fun toLocalRelatedPath(artifact: Artifact): String {
    val pathSegments = artifact.groupId.split("\\.").toMutableList()
    pathSegments.add(artifact.artifactId)
    pathSegments.add(artifact.version)

    val filename = with(artifact) {
      "$artifactId-$version${if (Strings.isBlank(classifier)) "" else "-$classifier"}.$packaging"
    }

    pathSegments.add(filename)
    return Strings.join(File.separator, pathSegments)
  }

  /**
   * 将工件信息转换为在本地仓库中的文件资源绝对路径
   */
  fun toLocalAbsolutePath(artifact: Artifact): String =
    File(localRepositoryRoot, toLocalRelatedPath(artifact)).absolutePath

  /**
   * 将pom内容解析为Maven Model
   */
  fun parsePomModel(content: String): Model {
    val sr = StringReader(content)
    val reader: Reader = BufferedReader(sr)
    val xpp3Reader = MavenXpp3Reader()
    return xpp3Reader.read(reader)
  }

  private fun invokeMavenGoal(goal: String) {
    val request: InvocationRequest = DefaultInvocationRequest()
    request.isBatchMode = true
    request.goals = listOf(goal)
    request.baseDirectory = tempDirectory

    val invoker: Invoker = DefaultInvoker()
    val result = try {
      invoker.execute(request)
    } catch (e: MavenInvocationException) {
      throw PluginMavenException(e)
    }

    if (result.executionException != null) {
      throw PluginMavenException(result.executionException, "Maven 执行错误")
    } else if (result.exitCode != 0) {
      // todo 判断各种退出码
      throw PluginMavenException("Maven 执行错误，退出代码：{}", result.exitCode)
    }
  }

  fun invokeGetGoal(artifact: Artifact) {
    val command = Strings.format(DEPENDENCY_GET_COMMAND, artifact.coordinate, artifact.packaging)
    invokeMavenGoal(command)
  }
}
