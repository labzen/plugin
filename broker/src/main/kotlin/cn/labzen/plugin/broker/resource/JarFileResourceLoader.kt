package cn.labzen.plugin.broker.resource

import cn.labzen.plugin.broker.exception.PluginResourceIOException
import cn.labzen.plugin.broker.maven.Mavens
import org.springframework.boot.loader.jar.JarFile
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.jar.JarEntry

open class JarFileResourceLoader(file: File) : FileResourceLoader(file) {

  private val jarFile = try {
    JarFile(file)
  } catch (e: IOException) {
    throw PluginResourceIOException(e, "插件Jar文件无法读取：{}", file.absoluteFile)
  }

  override fun associates(): List<URL> =
    jarFile.entries().toList().filter {
      !it.isDirectory && it.name.endsWith(".jar")
    }.map {
      URL(fileUrl, it.name)
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
