package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.broker.exception.PluginMavenException
import cn.labzen.plugin.broker.maven.Mavens
import java.io.File
import java.net.URL

internal class MavenDirectoryResourceLoader(directory: File) : DirectoryResourceLoader(directory) {

  private val pomFile: File
  private val pomResourceLoader: MavenPomFileResourceLoader

  init {
    pomFile = File(directory, Mavens.MAVEN_POM_XML_FILE)
    throwRuntimeUnless(pomFile.exists() && pomFile.isFile) {
      PluginMavenException("Maven项目POM文件不存在：{}", pomFile.absoluteFile)
    }

    val pomArtifact = Mavens.parsePomFileToArtifact(pomFile)
    pomResourceLoader = MavenPomFileResourceLoader(pomArtifact)
  }

  override fun associates(): Set<URL> {
    return pomResourceLoader.associates()
  }
}
