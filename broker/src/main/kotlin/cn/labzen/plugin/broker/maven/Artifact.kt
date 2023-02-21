package cn.labzen.plugin.broker.maven

import cn.labzen.plugin.broker.exception.PluginMavenException
import java.io.File
import java.net.URL

/**
 * Maven工件
 *
 * @property groupId GroupId
 * @property artifactId ArtifactId
 * @property version Version
 * @property packaging Maven Packaging
 * @property originalSource 工件的源文件地址
 * @property pomFileSource 工件的POM文件地址。如果：
 * 1. 本工件标识的是jar或bundle，并在maven本地仓库地址内，本属性可指向同级的pom文件定位
 * 2. 本工件标识的是jar或bundle，但属于独立的文件，无同级pom文件，本属性可指向jar包内的pom JarEntry
 * 3. 本工件标识的是pom，本属性与originalSource相同
 * @property pomFileContent 工件的POM文件内容
 */
data class Artifact @JvmOverloads constructor(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val packaging: Packaging = Packaging.JAR,
  internal var originalSource: URL? = null,
  internal var pomFileSource: URL? = null,
  internal var pomFileContent: String? = null
) {

  // Maven工件描述符
  val coordinate: String = Mavens.coordinateString(groupId, artifactId, version)
  var scope: Scope = Scope.COMPILE
  var classifier: String? = null

  /**
   * 工件重定向
   */
  var relocatedArtifact: Artifact? = null

  init {
    if (originalSource == null) {
      originalSource = if (packaging == Packaging.POM && pomFileSource != null) {
        pomFileSource
      } else {
        val sourceFileLocation = Mavens.toLocalAbsolutePath(this)
        URL("file:$sourceFileLocation")
      }
    }
  }

  /**
   * 工件指向的（本地）资源定位符
   */
  fun originalSource() =
    relocatedArtifact?.originalSource ?: originalSource

  /**
   * 源文件是否存在
   */
  fun originalSourceExists(): Boolean =
    originalSource?.let { File(it.toURI()).exists() } ?: false

  fun pomFileLoaded(): Boolean =
    pomFileContent != null || (pomFileSource?.let { File(it.toURI()).exists() } ?: false)

  // =================================================================

  fun setScope(value: String) {
    scope = Scope.parse(value) ?: throw PluginMavenException("非法的Maven坐标scope取值：{}", value)
  }

  fun isJarFile(): Boolean =
    packaging == Packaging.JAR || packaging == Packaging.BUNDLE

  fun isPomFile(): Boolean =
    packaging == Packaging.POM

  fun advanced(): AdvancedArtifact = AdvancedArtifact(this)

  enum class Packaging(val value: String) {
    JAR("jar"),
    POM("pom"),
    WAR("war"),
    EAR("ear"),
    EJB("ejb"),
    EJB_CLIENT("ejb-client"),
    MAVEN_PLUGIN("maven-plugin"),
    BUNDLE("bundle");

    companion object {
      fun parse(value: String): Packaging =
        values().find { it.value == value } ?: JAR
    }
  }

  enum class Scope(val value: String) {
    COMPILE("compile"),
    COMPILE_PLUS_RUNTIME("compile+runtime"),
    TEST("test"),
    RUNTIME("runtime"),
    RUNTIME_PLUS_SYSTEM("runtime+system"),
    PROVIDED("provided"),
    SYSTEM("system"),
    IMPORT("import");

    companion object {
      fun parse(value: String): Scope? =
        values().find { it.value == value }
    }
  }
}
