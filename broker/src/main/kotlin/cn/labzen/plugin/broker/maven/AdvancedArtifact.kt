package cn.labzen.plugin.broker.maven

import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.maven.Mavens.MAVEN_POM_FILE_EXTENSION
import org.apache.maven.model.DistributionManagement
import org.springframework.boot.loader.jar.JarFile
import java.io.File
import java.nio.file.Files

class AdvancedArtifact internal constructor(private val artifact: Artifact) {

  fun getOriginalFile(): File? =
    if (artifact.packaging == Artifact.Packaging.POM) {
      artifact.pomFileSourcePath?.let { File(it) }
    } else {
      artifact.originalSource()?.let { File(it) }
    }

  @Throws(PluginMavenException::class)
  fun downloadIfNecessary() {
    if (!artifact.originalSourceExists()) {
      download()
    }

    if (!artifact.pomFileLoaded()) {
      loadPom()
    }

    checkRelocation()
  }

  /**
   * 从远程仓库中下载工件
   */
  private fun download() {
    Mavens.invokeDependencyGetGoal(artifact)
  }

  /**
   * 加载Jar包内Pom（或pom本身）的内容
   */
  private fun loadPom() {
    artifact.originalSourcePath ?: throw PluginMavenException("找不到引用的Artifact资源源文件：{}", artifact.coordinate)

    val pomSource = if (artifact.isPomFile()) {
      artifact.originalSourcePath!!
    } else {
      val jarPath = artifact.originalSourcePath!!
      // val pomPathInSameDir = jarPath.substringBeforeLast(".") + MAVEN_POM_FILE_EXTENSION
      // URL(pomPathInSameDir)
      jarPath.substringBeforeLast(".") + MAVEN_POM_FILE_EXTENSION
    }

    val pomFile = File(pomSource)
    if (pomFile.exists()) {
      val pomContent = Files.readString(pomFile.toPath())
      artifact.pomFileSourcePath = pomSource
      artifact.pomFileContent = pomContent
      return
    }

    // 如果Maven本地仓库工件Jar包同级目录下没有Pom文件，尝试在Jar包中找
    val sourceFile = File(artifact.originalSourcePath!!)
    val sourceJar = JarFile(sourceFile)
    val pomEntry = sourceJar.entries().toList().find {
      !it.isDirectory && it.name.endsWith(Mavens.MAVEN_POM_XML_FILE)
    }

    pomEntry ?: throw PluginMavenException("找不到工件[${artifact.coordinate}]对应的POM文件：{}", pomFile.absoluteFile)
    val data = ByteArray(pomEntry.size.toInt())
    sourceJar.getInputStream(pomEntry).use { `is` ->
      `is`.read(data)
      artifact.pomFileContent = String(data)
    }
  }

  private fun checkRelocation() {
    artifact.pomFileContent ?: throw PluginMavenException("找不到Artifact的Pom内容：{}", artifact.coordinate)

    val model = Mavens.parsePomModel(artifact.pomFileContent!!)
    val distributionManagement: DistributionManagement? = model.distributionManagement

    // 有些jar，通过DistributionManagement中的relocation，将jar包的坐标变更了，需要重新指向
    distributionManagement?.relocation?.also { r ->
      val p = Artifact.Packaging.parse(model.packaging)
      val relocatedArtifact = Artifact(r.groupId, r.artifactId, r.version, p)

      artifact.relocatedArtifact = relocatedArtifact
    }
  }
}
