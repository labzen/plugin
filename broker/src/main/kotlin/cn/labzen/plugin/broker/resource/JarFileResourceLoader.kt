package cn.labzen.plugin.broker.resource

import cn.labzen.cells.core.kotlin.throwRuntimeUnless
import cn.labzen.plugin.broker.exception.PluginResourceIOException
import cn.labzen.plugin.broker.exception.PluginResourceLoadException
import cn.labzen.plugin.broker.maven.Mavens
import cn.labzen.plugin.broker.impl.memoir.bean.MemoirContext
import org.springframework.boot.loader.jar.JarFile
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.jar.JarEntry

internal open class JarFileResourceLoader(
  file: File,
  private val context: MemoirContext? = null
) : FileResourceLoader(file) {

  private val jarFile = try {
    JarFile(file)
  } catch (e: IOException) {
    throw PluginResourceIOException(e, "插件Jar文件无法读取：{}", file.absoluteFile)
  }

  override fun associates(): Set<URL> =
    context?.let {
      val dependenciesDir = File(file.parentFile, context.dependencies)
      dependenciesDir.exists().throwRuntimeUnless {
        PluginResourceLoadException("插件 [${jarFile.name}] 的依赖包目录 [${dependenciesDir.name}] 不存在")
      }
      dependenciesDir.listFiles()
        ?.filter { file -> file.isFile && file.extension == "jar" }
        ?.map { file -> file.toURI().toURL() }?.toSet()
    } ?: run {
      jarFile.entries().toList().filter {
        !it.isDirectory && it.name.endsWith(".jar")
      }.map {
        URL(fileUrl, it.name)
      }.toSet()
    }

  protected fun findPomEntryInJar(): JarEntry? {
    val entries = jarFile.entries()
    return entries.toList().find {
      !it.isDirectory && it.name.endsWith(Mavens.MAVEN_POM_XML_FILE)
    }
  }

  protected fun readPomContentInJar(entry: JarEntry): String {
    val data = ByteArray(entry.size.toInt())
    jarFile.getInputStream(entry).use { `is` ->
      `is`.read(data)
      return String(data)
    }
  }

  override fun supportExtensions(): List<String> = SUPPORT_EXTENSIONS

  companion object {
    private val SUPPORT_EXTENSIONS = listOf("jar")
  }
}
