package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.logger.kotlin.logger
import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.maven.Artifact
import java.net.URL

internal class MavenJarFileResourceLoader(private val artifact: Artifact) :
  JarFileResourceLoader(artifact.let {
    val adv = it.advanced()
    adv.downloadIfNecessary()
    adv.getOriginalFile() ?: throw PluginMavenException("无法正确定位artifact的本地文件位置：{}", artifact.coordinate)
  }) {

  private var cachedAssociates: Set<URL>? = null

  init {
    throwRuntimeUnless(artifact.isJarFile()) {
      PluginMavenException("只能处理Jar文件")
    }
  }

  override fun associates(): Set<URL> {
    if (cachedAssociates != null) {
      return cachedAssociates!!
    }

    val advancedArtifact = artifact.advanced().also {
      it.downloadIfNecessary()
    }

    if (advancedArtifact.getOriginalFile()?.exists() == true) {
      val pomArtifact = with(artifact) {
        Artifact(
          groupId,
          artifactId,
          version,
          Artifact.Packaging.POM,
          null,
          artifact.pomFileSourcePath,
          artifact.pomFileContent
        )
      }

      val associates = MavenPomFileResourceLoader(pomArtifact).associates()
      cachedAssociates = associates
      return associates
    }

    // 找JAR包中的POM文件
    val pomEntry = findPomEntryInJar()
    if (pomEntry == null) {
      logger.debug("无法在JAR文件中找到POM文件: {}", artifact)
      return emptySet()
    }

    val pomContent = readPomContentInJar(pomEntry)
    val loader = MavenPomFileResourceLoader.createVirtualPomFileLoader(pomContent)
    return loader.associates()
  }

  companion object {
    private val logger = logger { }
  }
}
