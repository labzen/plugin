package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.maven.Artifact
import cn.labzen.plugin.broker.maven.Mavens
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import java.io.File
import java.net.URL
import java.nio.file.Files

class MavenPomFileResourceLoader(private val artifact: Artifact) :
  PomFileResourceLoader(artifact.let {
    val adv = it.advanced()
    adv.downloadIfNecessary()

    adv.getOriginalFile() ?: throw PluginMavenException(
      "无法正确定位artifact pom的本地文件位置：{}",
      artifact.coordinate
    )
  }) {

  private val model: Model
  private var parentResourceLoader: MavenPomFileResourceLoader? = null

  init {
    artifact.pomFileContent ?: throw PluginMavenException("无法获取artifact pom信息：{}", artifact.coordinate)
    model = Mavens.parsePomModel(artifact.pomFileContent!!)
  }

  override fun associates(): List<URL> {
    return model.dependencies.filter(this::shouldBeGet).flatMap(this::getAssociatedDependency)
  }

  private fun shouldBeGet(dependency: Dependency): Boolean =
    Strings.isBlank(dependency.scope) ||
        dependency.scope.let {
          val scope = Artifact.Scope.parse(it)
          scope == Artifact.Scope.COMPILE || scope == Artifact.Scope.RUNTIME || scope == Artifact.Scope.COMPILE_PLUS_RUNTIME
        } &&
        dependency.type.let {
          val packaging = Artifact.Packaging.parse(it)
          packaging == Artifact.Packaging.JAR || packaging == Artifact.Packaging.POM || packaging == Artifact.Packaging.BUNDLE
        }

  /**
   * 获取当前工件（POM）所依赖的其他工件
   */
  private fun getAssociatedDependency(dependency: Dependency): List<URL> {
    accurateGroupId(dependency)
    accurateArtifactId(dependency)
    accurateVersion(dependency)

    val packaging = Artifact.Packaging.parse(dependency.type)
    val artifact = Artifact(dependency.groupId, dependency.artifactId, dependency.version, packaging).also {
      it.classifier = dependency.classifier
    }

    val associates = mutableSetOf<URL>()
    val dependencyResourceLoader = MavenJarFileResourceLoader(artifact)
    associates.add(dependencyResourceLoader.getUrl())
    associates.addAll(dependencyResourceLoader.associates())
    return associates.toList()
  }

  /**
   * 精确依赖工件的groupId
   */
  private fun accurateGroupId(dependency: Dependency) {
    if (!isFuzzyReferenceValue(dependency.groupId)) {
      return
    }

    // 只尝试在当前POM中的 properties 中的 group id 定义
    // 先不找那么深了，就在当前文件中找找得了
    val groupId = valueInProperties(dependency.groupId)

    if (isFuzzyReferenceValue(groupId)) {
      throw PluginMavenException(
        "无法解析Dependency的GroupId：{}:{}:{}",
        groupId,
        dependency.artifactId,
        dependency.version
      )
    } else {
      dependency.groupId = groupId
    }
  }

  /**
   * 精确依赖工件的artifactId
   */
  private fun accurateArtifactId(dependency: Dependency) {
    if (!isFuzzyReferenceValue(dependency.artifactId)) {
      return
    }

    // 只尝试在当前POM中的 properties 中的 group id 定义
    // 先不找那么深了，就在当前文件中找找得了
    val artifactId = valueInProperties(dependency.artifactId)

    if (isFuzzyReferenceValue(artifactId)) {
      throw PluginMavenException(
        "无法解析Dependency的ArtifactId：{}:{}:{}",
        dependency.groupId,
        artifactId,
        dependency.version
      )
    } else {
      dependency.artifactId = artifactId
    }
  }

  /**
   * 精确依赖工件的version
   */
  private fun accurateVersion(dependency: Dependency) {
    if (!isFuzzyReferenceValue(dependency.version)) {
      return
    }

    val version = findVersion(dependency, dependency.version)
    if (isFuzzyReferenceValue(version)) {
      throw PluginMavenException(
        "无法解析Dependency的GroupId：{}:{}:{}",
        dependency.groupId,
        dependency.artifactId,
        version
      )
    } else {
      dependency.version = version
    }
  }

  /**
   * 在pom文件中尝试找到真实的依赖工件版本
   */
  private fun findVersion(dependency: Dependency, fuzzyReferenceValue: String?): String? {
    // 先尝试在当前POM中的 properties 中的版本定义
    var realVersion = valueInProperties(fuzzyReferenceValue)

    // 如果没有找到，看当前POM中的DependencyManagement定义
    if (isFuzzyReferenceValue(realVersion)) {
      val dependencyManagement = model.dependencyManagement
      // 没有DependencyManagement定义，忽略这种途径
      if (dependencyManagement != null) {
        val managedDependency = dependencyManagement.dependencies.firstOrNull {
          it.groupId == dependency.groupId && it.artifactId == dependency.artifactId
        }

        managedDependency?.run {
          realVersion = version
          // 防止 DependencyManagement 中定义的版本号有可能也是模糊的（只可能是${}形式的property引用）
          realVersion = valueInProperties(realVersion)
        }
      }
    }

    // 如果还不行，看 DependencyManagement 中有没有 import 的 pom 文件
    if (isFuzzyReferenceValue(realVersion)) {
      realVersion = findVersionInManagementImport(dependency, realVersion)
    }
    // 如果没有找到，向 parent pom 中查找版本配置
    if (isFuzzyReferenceValue(realVersion)) {
      realVersion = findVersionInParentPom(dependency, realVersion)
    }

    // 防止一万个万一，再他妈去找一遍，算是最后的挣扎吧
    realVersion = valueInProperties(realVersion)
    return realVersion
  }

  /**
   * 在 DependencyManagement 中使用 import scope 引入的 其他pom文件，在这里边找找吧，艹
   */
  private fun findVersionInManagementImport(dependency: Dependency, fuzzyReferenceValue: String?): String? {
    val dependencyManagement = model.dependencyManagement ?: return fuzzyReferenceValue

    val importedPoms = dependencyManagement.dependencies.filter {
      Artifact.Scope.IMPORT.value.equals(it.scope, true)
    }
    if (importedPoms.isEmpty()) {
      // 没有导入的POM
      return fuzzyReferenceValue
    }

    var valueFromImportedBom = fuzzyReferenceValue
    for (bom in importedPoms) {
      // importedBomVersion 可能也不是标准版本，这里只是从当前pom中获取引入bom的版本，极端情况下，引入bom的版本可能在父级pom内定义，这里暂不做复杂情况的兼容处理
      val importedBomVersion = valueInProperties(bom.version) ?: continue

      val pomArtifact = Artifact(bom.groupId, bom.artifactId, importedBomVersion, Artifact.Packaging.POM)

      val pomLoader = MavenPomFileResourceLoader(pomArtifact)
      valueFromImportedBom = pomLoader.findVersion(dependency, fuzzyReferenceValue)
      if (!isFuzzyReferenceValue(valueFromImportedBom)) {
        break
      }
    }
    return valueFromImportedBom
  }

  /**
   * 从父pom中寻找版本
   */
  private fun findVersionInParentPom(dependency: Dependency, fuzzyReferenceValue: String?): String? {
    loadParentIfNecessary()
    return parentResourceLoader?.findVersion(dependency, fuzzyReferenceValue) ?: fuzzyReferenceValue
  }

  /**
   * 加载父pom
   */
  private fun loadParentIfNecessary() {
    if (parentResourceLoader != null) {
      return
    }
    val parent = model.parent ?: return

    val parentArtifact = Artifact(parent.groupId, parent.artifactId, parent.version, Artifact.Packaging.POM)
    parentResourceLoader = MavenPomFileResourceLoader(parentArtifact)
  }

  /**
   * 从pom文件中的properties段中读取值
   */
  private fun valueInProperties(key: String?): String? {
    key ?: return null

    val matched = PROPERTY_REFERENCE_PATTERN.matchEntire(key)
    matched ?: return key

    // 判断 0.3.10-${javacpp.version} 这样变态的版本号
    val prefix = matched.groupValues[1]
    val ref = matched.groupValues[2]

    // maven的系统 property
    return if (ref.startsWith(MAVEN_PROJECT_PROPERTY_PREFIX)) {
      val prop = readMavenProjectProperty(ref)
      prop?.let { prefix + it } ?: key
    } else {
      val prop: String = model.properties.getProperty(ref, key)
      if (key == prop) {
        // 反正意思就是没找着
        key
      } else {
        /* 尝试再找一遍，防止变态写两重property引用
        * 就是这样式儿的：
        * <javacpp.platform.extension></javacpp.platform.extension>
        * <javacpp.platform.android-arm>android-arm${javacpp.platform.extension}</javacpp.platform.android-arm>
        *
        * 还有一次是这样式儿的：
        * 妈的，spring-boot-dependencies.pom里有这么一行property: <jackson-bom.version>${jackson.version}</jackson-bom.version>
        * 什么样的神经病这么定义，啊？？？？
        */
        if (isFuzzyReferenceValue(prop)) {
          prefix + valueInProperties(prop)
        } else {
          prefix + prop
        }
      }
    }
  }

  /**
   * 读取maven project系统属性
   */
  private fun readMavenProjectProperty(key: String): String? {
    return when (key) {
      "project.groupId" -> model.groupId
      "project.version" -> if (model.version != null) {
        model.version
      } else if (model.parent != null) {
        // 喵了个咪的，真的会有这样搞的，org.bytedeco:javacv-platform 没有定义version，通过继承parent，不大合适吧你
        model.parent.version
      } else {
        // 我擦，这就更变态了，但愿别有哪个傻子这么搞，应该不会吧
        ""
      }

      "project.parent.version" -> if (model.parent != null) {
        model.parent.version
      } else {
        /* 我擦，没招了，org.bytedeco:openblas 的parent是 org.bytedeco:javacpp-presets 。
         * 他喵的，这个pom里边对 org.bytedeco:javacpp 的 dependencyManagement 的版本定义是：${project.parent.version}
         * 艹了蛋了，你喵的没有parent好不好！！
         * 我真没招了，我也不能猜一个版本啊，只能用自身的版本了，先这样吧，妈的变态，你个小可爱
         */
        model.version
      }

      else -> ""
    }
  }

  /**
   * 判断是不是一个引用值 ${} 这样儿的
   */
  private fun isFuzzyReferenceValue(value: String?): Boolean =
    Strings.isBlank(value) || value?.matches(PROPERTY_REFERENCE_PATTERN) == true

  companion object {
    private const val MAVEN_PROJECT_PROPERTY_PREFIX = "project."
    private val PROPERTY_REFERENCE_PATTERN = Regex("^(.*)\\$\\{(.*)}$")

    fun createVirtualPomFileLoader(content: String): MavenPomFileResourceLoader {
      val tempPomFile = File.createTempFile("pom", ".pom")
      Files.writeString(tempPomFile.toPath(), content)

      val artifact = Mavens.parsePomFileToArtifact(tempPomFile)
      return MavenPomFileResourceLoader(artifact)
    }
  }
}
