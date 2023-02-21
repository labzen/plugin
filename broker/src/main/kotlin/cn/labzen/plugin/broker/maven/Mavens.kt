package cn.labzen.plugin.broker.maven

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.cells.core.utils.Strings
import cn.labzen.logger.kotlin.logger
import cn.labzen.meta.Labzens
import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.meta.PluginBrokerConfiguration
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.shared.invoker.*
import java.io.BufferedReader
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.URL
import java.nio.file.Files

object Mavens {

  private val logger = logger { }
  private lateinit var mavenHome: File
  private lateinit var mavenRemoteRepository: String
  private lateinit var mavenLocalRepository: File
  private lateinit var mavenDependencyGetCommand: String

  private val tempDirectory: File = File(Labzens.environment().ioTempPath)

  const val MAVEN_JAR_FILE_EXTENSION = ".jar"
  const val MAVEN_POM_FILE_EXTENSION = ".pom"
  const val MAVEN_XML_FILE_EXTENSION = ".xml"
  const val MAVEN_POM_XML_FILE = "pom.xml"

  internal fun initialize() {
    val configuration = Labzens.configurationWith(PluginBrokerConfiguration::class.java)
    mavenHome = File(configuration.mavenHome())
    mavenRemoteRepository = configuration.mavenRemoteRepositoryUri()
    mavenLocalRepository = configuration.mavenLocalRepositoryLocation()?.let { File(it) }
      ?: File(Labzens.environment().userHome, ".m2${File.separator}repository")
    mavenDependencyGetCommand =
      "org.apache.maven.plugins:maven-dependency-plugin:${configuration.mavenPluginDependencyVersion()}:get " +
          "-DremoteRepositories=custom::default::$mavenRemoteRepository -Dartifact={}"

    mavenHome.exists().throwRuntimeUnless { PluginMavenException("Maven Home: ${configuration.mavenHome()} 不存在") }

    // todo 验证本机是否有可用Maven，以及版本
    if (mavenLocalRepository.exists()) {
      logger.info("Plugin Broker将使用Maven本地仓库地址：{}", mavenLocalRepository.absoluteFile)
    } else {
      logger.warn("Plugin Broker找不到Maven本地仓库目录: {}", mavenLocalRepository.path)
    }

    // todo 使用配置的方式传入本地仓库地址
  }

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
    return Artifact(model.artifactId, model.groupId, model.version, packaging, null, originalPomFile)
  }

  fun parsePomFileToArtifact(file: File): Artifact {
    val content = Files.readString(file.toPath())
    val model = parsePomModel(content)
    return Artifact(
      model.groupId,
      model.artifactId,
      model.version,
      Artifact.Packaging.POM,
      null,
      file.toURI().toURL(),
      content
    )
  }

  /**
   * 将工件信息转换为相对于本地仓库根目录的工具文件资源相对路径
   */
  private fun toLocalRelatedPath(artifact: Artifact): String {
    val pathSegments = artifact.groupId.split(".").toMutableList()
    pathSegments.add(artifact.artifactId)
    pathSegments.add(artifact.version)

    val filename = with(artifact) {
      "$artifactId-$version${if (Strings.isBlank(classifier)) "" else "-$classifier"}.${packaging.value}"
    }

    pathSegments.add(filename)
    return Strings.join(File.separator, pathSegments)
  }

  /**
   * 将工件信息转换为在本地仓库中的文件资源绝对路径
   */
  fun toLocalAbsolutePath(artifact: Artifact): String =
    File(mavenLocalRepository, toLocalRelatedPath(artifact)).absolutePath

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
    request.mavenHome = mavenHome
    request.isBatchMode = false
    request.goals = listOf(goal)
    request.baseDirectory = tempDirectory

    val invoker: Invoker = DefaultInvoker()
    logger.info("执行Maven命令 mvn $goal")
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

  fun invokeDependencyGetGoal(artifact: Artifact) {
    val command = Strings.format(mavenDependencyGetCommand, artifact.coordinate).also {
      if (artifact.packaging == Artifact.Packaging.POM) {
        "$it -Dpackaging=pom"
      } else it
    }

    invokeMavenGoal(command)
  }
}
