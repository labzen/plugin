package cn.labzen.plugin.broker.maven

import cn.labzen.plugin.broker.exception.PluginMavenException
import java.io.File
import java.net.URL

data class Artifact @JvmOverloads constructor(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val packaging: Packaging? = Packaging.JAR
) {

  constructor(
    groupId: String,
    artifactId: String,
    version: String,
    packaging: Packaging? = Packaging.JAR,
    source: URL? = null,
    content: String? = null
  ) : this(groupId, artifactId, version, packaging) {
    originalSource = source
    pomContent = content

    if (packaging == Packaging.POM) {
      pomSource = originalSource
    }

    originalExists = originalSource?.let {
      File(it.toURI()).exists()
    } ?: false
    pomLoaded = pomSource?.let {
      File(it.toURI()).exists()
    } ?: false && content != null
  }

  val coordinate: String = Mavens.coordinateString(groupId, artifactId, version)
  var scope: Scope = Scope.COMPILE
  var classifier: String? = null

  /**
   * 工件指向的（本地）资源定位符
   */
  var originalSource: URL? = null
    get() {
      return relocatedArtifact?.originalSource ?: field
    }

  /**
   * 工件的pom（本地）资源定位符，如果：
   *
   * 1. 本工件标识的是jar或bundle，并在maven本地仓库地址内，本属性可指向同级的pom文件定位
   * 2. 本工件标识的是jar或bundle，但属于独立的文件，无同级pom文件，本属性可指向jar包内的pom JarEntry
   * 3. 本工件标识的是pom，本属性与originalSource相同
   */
  var pomSource: URL? = null

  /**
   * pom文件中的内容
   */
  var pomContent: String? = null

  /**
   * 工件重定向
   */
  var relocatedArtifact: Artifact? = null

  internal var originalExists: Boolean = false
  internal var pomLoaded: Boolean = false
  internal var relocateChecked: Boolean = false

  // =================================================================

  fun setScope(value: String) {
    scope = Scope.parse(value) ?: throw PluginMavenException("非法的Maven坐标scope取值：{}", value)
  }

  fun isJarFile(): Boolean =
    packaging == null || packaging == Packaging.JAR || packaging == Packaging.BUNDLE

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
