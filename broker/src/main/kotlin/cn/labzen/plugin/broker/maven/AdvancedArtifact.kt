package cn.labzen.plugin.broker.maven

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.maven.Mavens.MAVEN_POM_FILE_EXTENSION
import org.apache.maven.model.DistributionManagement
import java.io.File
import java.net.URL
import java.nio.file.Files

class AdvancedArtifact internal constructor(private val artifact: Artifact) {

  fun getOriginalFile(): File? =
    artifact.originalSource?.let {
      File(it.toURI())
    }

  @Throws(PluginMavenException::class)
  fun downloadIfNecessary() {
    if (!artifact.originalExists) {
      download()
    }

    if (!artifact.pomLoaded) {
      loadPom()
    }

    if (!artifact.relocateChecked) {
      checkRelocation()
    }
  }

  /**
   * 从远程仓库中下载工件
   */
  private fun download() {
    val localPath = Mavens.toLocalAbsolutePath(artifact)
    val file = File(localPath)
    if (file.exists()) {
      return
    }

    Mavens.invokeGetGoal(artifact)
    artifact.originalSource = file.toURI().toURL()
    artifact.originalExists = true
  }

  /**
   * 加载Jar包内Pom（或pom本身）的内容
   */
  private fun loadPom() {
    artifact.originalSource ?: throw PluginMavenException("找不到引用的Artifact资源源文件：{}", artifact.coordinate)

    val pomSource = if (artifact.isPomFile()) {
      artifact.originalSource!!
    } else {
      val jarPath = artifact.originalSource!!.toExternalForm()
      val pomPathInSameDir = jarPath.dropLastWhile { it == '.' } + MAVEN_POM_FILE_EXTENSION
      URL(pomPathInSameDir)
    }

    val pomFile = File(pomSource.toURI())
    throwRuntimeUnless(pomFile.exists()) {
      PluginMavenException("找不到对应的POM文件：{}", pomFile.absoluteFile)
    }

    val pomContent = Files.readString(pomFile.toPath())
    artifact.pomSource = pomSource
    artifact.pomContent = pomContent
    artifact.pomLoaded = true
  }

  private fun checkRelocation() {
    artifact.pomContent ?: throw PluginMavenException("找不到Artifact的Pom内容：{}", artifact.coordinate)

    val model = Mavens.parsePomModel(artifact.pomContent!!)
    val distributionManagement: DistributionManagement? = model.distributionManagement

    // 有些jar，通过DistributionManagement中的relocation，将jar包的坐标变更了，需要重新指向
    distributionManagement?.relocation?.also { r ->
      val p = Artifact.Packaging.parse(model.packaging)
      val relocatedArtifact = Artifact(r.groupId, r.artifactId, r.version, p)

      artifact.relocatedArtifact = relocatedArtifact
    }
    artifact.relocateChecked = true
  }
}
