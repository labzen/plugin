package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import java.io.File
import java.net.URL

internal abstract class DirectoryResourceLoader(private val directory: File) : ResourceLoader {

  private val directoryUrl: URL = directory.toURI().toURL()

  init {
    throwRuntimeUnless(directory.exists() && directory.isDirectory) {
      PluginResourceLoadException("Maven项目目录资源不存在：{}", directory.absoluteFile)
    }
  }

  override fun getUrl(): URL = directoryUrl
}
