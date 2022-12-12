package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import cn.labzen.plugin.broker.maven.Mavens.MAVEN_JAR_FILE_EXTENSION
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

open class DirectoryResourceLoader(private val directory: File) : ResourceLoader {

  protected val directoryUrl: URL = directory.toURI().toURL()

  init {
    throwRuntimeUnless(directory.exists() && directory.isDirectory) {
      PluginResourceLoadException("Maven项目目录资源不存在：{}", directory.absoluteFile)
    }
  }

  override fun getUrl(): URL = directoryUrl

  override fun associates(): List<URL> {
    val path = directory.toPath()
    return Files.find(path, Int.MAX_VALUE, { p, _ ->
      p.fileName.endsWith(MAVEN_JAR_FILE_EXTENSION)
    }).map(this::pathToUrl).collect(Collectors.toUnmodifiableList())
  }

  private fun pathToUrl(path: Path): URL =
    path.toUri().toURL()
}
