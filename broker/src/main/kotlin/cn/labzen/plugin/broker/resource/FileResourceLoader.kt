package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.cells.core.utils.Collections
import cn.labzen.cells.core.utils.Strings
import cn.labzen.plugin.broker.exception.PluginResourceInvalidException
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import java.io.File
import java.net.URL

open class FileResourceLoader(private val file: File) : ResourceLoader {

  protected val fileUrl: URL = file.toURI().toURL()

  init {
    throwRuntimeUnless(file.exists() && file.isFile) {
      PluginResourceLoadException("文件资源不存在：{}", file.absoluteFile)
    }

    checkFilename()
    checkExtension()
  }

  override fun getUrl(): URL = fileUrl

  override fun associates(): List<URL> = emptyList()

  /**
   * 基于文件资源加载的，文件扩展限定，范围外的文件将加载异常，默认空集合，意为不限
   */
  protected open fun supportExtensions(): List<String>? = null

  /**
   * 严格匹配文件名
   */
  open fun strictFilenamePattern(): String? = null

  private fun checkFilename() {
    val strictPattern = strictFilenamePattern()
    if (Strings.isBlank(strictPattern)) {
      return
    }

    val matched = file.name.matches(Regex(strictPattern!!))
    if (!matched) {
      throw PluginResourceInvalidException("文件资源名不符：{}, expect: {}", file.name, strictPattern)
    }
  }

  private fun checkExtension() {
    val supportExtensions = supportExtensions()
    if (Collections.isNullOrEmpty(supportExtensions)) {
      return
    }

    val name = file.name
    for (index in name.lastIndex downTo 0) {
      if (name[index] == '.') {
        val ext = name.substring(index + 1)
        if (supportExtensions!!.contains(ext)) {
          return
        }
      }
    }

    throw PluginResourceInvalidException("文件资源[{}]扩展名被限定为：{}", name, supportExtensions!!.joinToString(", "))
  }

}
